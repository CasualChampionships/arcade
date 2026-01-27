/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.nametags.virtual

import com.google.common.collect.Iterables
import com.google.common.collect.LinkedHashMultimap
import it.unimi.dsi.fastutil.objects.Reference2ReferenceLinkedOpenHashMap
import net.casual.arcade.nametags.Nametag
import net.casual.arcade.nametags.extensions.EntityNametagExtension.Companion.nametagExtension
import net.casual.arcade.virtual.entity.VirtualEntity
import net.casual.arcade.virtual.entity.attachment.RootVirtualEntityAttachment
import net.casual.arcade.virtual.entity.attachment.anchor.EntityAttachmentAnchor
import net.casual.arcade.virtual.entity.tracker.ObserverTracker
import net.casual.arcade.virtual.entity.tracker.SimpleObserverTracker
import net.casual.arcade.virtual.entity.utils.startObservingAndSendPackets
import net.casual.arcade.virtual.entity.utils.stopObservingAndSendPackets
import net.minecraft.network.protocol.Packet
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl
import net.minecraft.world.entity.Entity
import net.casual.arcade.utils.ClientboundSetPassengersPacket as createSetPassengersPacket

public class NametagVirtualEntityAttachment(
    override val anchor: EntityAttachmentAnchor,
): RootVirtualEntityAttachment {
    private val nametags = Reference2ReferenceLinkedOpenHashMap<Nametag, NametagVirtualEntity>()
    private val tracked = LinkedHashMultimap.create<ServerGamePacketListenerImpl, NametagVirtualEntity>()

    override val observers: ObserverTracker = SimpleObserverTracker()

    private val root = NametagHeightVirtualEntity(
        this, this.observers, NametagHeight.INITIAL, RetargetingInteractionHandler(this.entity)
    )

    private val entity: Entity
        get() = this.anchor.entity

    public fun attach(nametag: Nametag) {
        val entity = NametagVirtualEntity(this, this.entity, nametag)
        this.nametags[nametag] = entity

        entity.updateNametag()
    }

    public fun detach(nametag: Nametag) {
        val entity = this.nametags.remove(nametag) ?: return
        entity.observers.broadcast { observer, consumer ->
            entity.stopObservingAndSendPackets(observer, consumer)
            val tracked = this.tracked[observer.connection]
            if (!tracked.isEmpty()) {
                tracked.remove(entity)
                this.resendNametagStackFor(observer, consumer, tracked, false)
            }
        }
    }

    public fun detachAll() {
        for (entity in this.nametags.values) {
            entity.observers.broadcast { observer, consumer ->
                entity.stopObservingAndSendPackets(observer, consumer)
            }
        }
        this.tracked.clear()
        this.nametags.clear()
    }

    public fun sneak() {
        for (entity in this.nametags.values)  {
            entity.sneak()
        }
    }

    public fun unsneak() {
        for (entity in this.nametags.values)  {
            entity.unsneak()
        }
    }

    public fun getNametags(): Set<Nametag> {
        return this.nametags.keys
    }

    public fun getNametagEntities(): Collection<NametagVirtualEntity> {
        return this.nametags.values
    }

    public fun getRootId(): Int {
        return this.root.id
    }

    override fun tick() {
        this.observers.broadcast(this::updateObserver)

        super.tick()
    }

    override fun attach(entity: VirtualEntity): Boolean {
        return false
    }

    override fun detach(entity: VirtualEntity): Boolean {
        return false
    }

    override fun attached(): Iterable<VirtualEntity> {
        return Iterables.concat(this.nametags.values, listOf(this.root))
    }

    public fun isObservingNonEmpty(observer: ServerPlayer): Boolean {
        return this.tracked[observer.connection].isNotEmpty()
    }

    private fun updateObserver(observer: ServerPlayer, consumer: (Packet<*>) -> Unit) {
        val connection = observer.connection
        val tracked = this.tracked.get(connection)
        val wasStackEmpty = tracked.isEmpty()
        var dirty = false
        for ((nametag, entity) in this.nametags) {
            val watching = entity.observers.isObserving(observer)

            val canWatch = this.entity.broadcastToPlayer(connection.player)
                && nametag.isObservable(this.entity, connection.player)
                && nametag.isWithinRange(this.entity, connection.player)

            if (watching) {
                if (!canWatch) {
                    tracked.remove(entity)
                    entity.stopObservingAndSendPackets(observer, consumer)
                    dirty = true
                }
            } else if (canWatch) {
                tracked.add(entity)
                entity.startObservingAndSendPackets(observer, consumer)
                dirty = true
            }
        }

        if (dirty) {
            this.resendNametagStackFor(observer, consumer, tracked, wasStackEmpty)
        }
    }

    private fun resendNametagStackFor(
        observer: ServerPlayer,
        consumer: (Packet<*>) -> Unit,
        stack: Collection<NametagVirtualEntity>,
        wasStackEmpty: Boolean
    ) {
        if (stack.isEmpty()) {
            this.root.sendDespawnPackets(observer, consumer)
            return
        }
        if (wasStackEmpty) {
            this.root.sendSpawnPackets(observer, consumer)
        }

        var previous = this.root.id
        for (entity in this.nametags.values.reversed()) {
            if (!stack.contains(entity)) {
                continue
            }

            val packet = createSetPassengersPacket(previous, entity.getPassengerIds())
            consumer.invoke(packet)

            previous = entity.getVehicleId()
        }

        this.entity.nametagExtension.broadcastUpdatePassengersPacket(this.observers, this.root)
    }
}