/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity

import net.casual.arcade.virtual.entity.attachment.VirtualEntityAttachment
import net.casual.arcade.virtual.entity.interaction.EntityInteraction
import net.casual.arcade.virtual.entity.location.VirtualPosition
import net.casual.arcade.virtual.entity.location.VirtualRotation
import net.casual.arcade.virtual.entity.mixins.EntityAccessor
import net.casual.arcade.virtual.entity.utils.location
import net.minecraft.network.protocol.Packet
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

    public fun tick()

    public fun startObserving(observer: ServerPlayer): Boolean

    public fun stopObserving(observer: ServerPlayer): Boolean

    public fun isObserving(observer: ServerPlayer): Boolean

    public fun sendSpawnPackets(observer: ServerPlayer, consumer: (Packet<*>) -> Unit)

    public fun sendDespawnPackets(observer: ServerPlayer, consumer: (Packet<*>) -> Unit)

    public fun canObserve(observer: ServerPlayer): Boolean {
        return this.location().position.closerThan(observer.position(), this.observableRange())
    }

    public fun observableRange(): Double {
        return DEFAULT_OBSERVABLE_RANGE
    }

    public fun getInteractionHandler(player: ServerPlayer): InteractionHandler? {
        return null
    }

    public fun interface InteractionHandler {
        public fun interact(player: ServerPlayer, interaction: EntityInteraction)
    }

    public companion object {
        public const val DEFAULT_OBSERVABLE_RANGE: Double = 64.0

        @JvmStatic
        public fun getNextEntityId(): Int {
            return EntityAccessor.accessEntityCounter().incrementAndGet()
        }
    }
}