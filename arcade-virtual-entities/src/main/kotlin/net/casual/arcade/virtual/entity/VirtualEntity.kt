/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity

import net.casual.arcade.networking.observer.Observer
import net.casual.arcade.networking.observer.tracker.ObserverTracker
import net.casual.arcade.networking.observer.tracker.ParentObserverTracker
import net.casual.arcade.networking.packet.PacketSender
import net.casual.arcade.utils.math.location.closerThan
import net.casual.arcade.virtual.entity.attachment.VirtualEntityAttachment
import net.casual.arcade.virtual.entity.interaction.EntityInteraction
import net.casual.arcade.virtual.entity.location.VirtualPosition
import net.casual.arcade.virtual.entity.location.VirtualRotation
import net.casual.arcade.virtual.entity.mixins.ServerLevelAccessor
import net.casual.arcade.virtual.entity.utils.location
import net.casual.arcade.virtual.entity.utils.startObservingAndSendPackets
import net.casual.arcade.virtual.entity.utils.stopObservingAndSendPackets
import net.minecraft.server.level.ServerPlayer
import java.util.*

/**
 * This interface represents a virtual entity.
 *
 * Virtual entities are entities which can be fully customized
 * on the server, without needing to create real [net.minecraft.world.entity.Entity]
 * instances, but behave like real entities on the client.
 *
 * These are ideal if you need to spawn entities
 * that have minimal impact on server performance,
 * as they do not have all the overhead of real entities,
 * for example displaying custom models.
 * Or, for example, you could compose a real entity
 * out of multiple virtual entities.
 *
 * @see SimpleVirtualEntity
 */
public interface VirtualEntity {
    /**
     * The virtual entity's attachment.
     */
    public val attachment: VirtualEntityAttachment

    /**
     * The observer tracker for this virtual entity.
     */
    public val observers: ObserverTracker

    /**
     * The virtual entity's id.
     */
    public val id: Int

    /**
     * Ths virtual entity's uuid.
     */
    public val uuid: UUID

    /**
     * The virtual entity's position.
     */
    public var position: VirtualPosition

    /**
     * The virtual entity's rotation.
     */
    public var rotation: VirtualRotation

    /**
     * The virtual entity's tick function,
     * called once per game tick.
     *
     * When exactly this function is called
     * depends on the [VirtualEntityAttachment].
     */
    public fun tick()

    /**
     * This function sends this virtual entity's spawn packets
     * to the specified [observer].
     *
     * This function should be *stateless*.
     * Callers should call [stopObservingAndSendPackets].
     *
     * @param observer The player to send spawn packets to.
     * @param sender The packet sender.
     * @see startObservingAndSendPackets
     */
    public fun sendSpawnPackets(observer: Observer, sender: PacketSender)

    /**
     * This function sends this virtual entity's despawn packets
     * to the specified [observer].
     *
     * Typically, this is just [net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket].
     *
     * This function should be *stateless*.
     * Callers should call [stopObservingAndSendPackets].
     *
     * @param observer The player to send despawn packets to.
     * @param sender The packet sender.
     * @see stopObservingAndSendPackets
     */
    public fun sendDespawnPackets(observer: Observer, sender: PacketSender)

    /**
     * Checks whether an [observer] can observe this virtual entity.
     *
     * @param observer The player trying to observe.
     * @return Whether the [observer] can observe.
     */
    public fun canObserve(observer: Observer): Boolean {
        if (this.observers is ParentObserverTracker) {
            return this.observers.isObserving(observer)
        }

        return observer.location.closerThan(this.location(), this.getObservableRange())
    }

    /**
     * The range at which this virtual entity is observable.
     *
     * @return The observable range.
     */
    public fun getObservableRange(): Double {
        return DEFAULT_OBSERVABLE_RANGE
    }

    /**
     * This gets an [InteractionHandler] for the given [player].
     *
     * @param player The player who interacted with this virtual entity.
     * @return The interaction handler, null for no interaction.
     */
    public fun getInteractionHandler(player: ServerPlayer): InteractionHandler? {
        return null
    }

    public fun interface InteractionHandler {
        public fun interact(player: ServerPlayer, interaction: EntityInteraction)

        public companion object {
            public inline fun <reified T: EntityInteraction> only(
                crossinline handler: (ServerPlayer, T) -> Unit
            ): InteractionHandler {
                return InteractionHandler { player, interaction ->
                    if (interaction is T) {
                        handler.invoke(player, interaction)
                    }
                }
            }
        }
    }

    public companion object {
        public const val DEFAULT_OBSERVABLE_RANGE: Double = 64.0

        @JvmStatic
        public fun getNextEntityId(): Int {
            return ServerLevelAccessor.accessEntityCounter().incrementAndGet()
        }
    }
}