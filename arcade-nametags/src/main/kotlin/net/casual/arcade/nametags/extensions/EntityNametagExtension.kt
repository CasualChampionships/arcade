/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.nametags.extensions

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.player.PlayerClientboundPacketEvent
import net.casual.arcade.events.server.player.PlayerClientboundPacketEvent.Companion.replacePacket
import net.casual.arcade.events.server.player.PlayerPoseEvent
import net.casual.arcade.events.utils.register
import net.casual.arcade.extensions.EntityExtension
import net.casual.arcade.extensions.Extension
import net.casual.arcade.extensions.event.EntityExtensionEvent
import net.casual.arcade.extensions.utils.getExtension
import net.casual.arcade.nametags.Nametag
import net.casual.arcade.nametags.virtual.NametagVirtualEntity
import net.casual.arcade.nametags.virtual.NametagVirtualEntityAttachment
import net.casual.arcade.utils.asClientGamePacket
import net.casual.arcade.utils.compat.PolymerCompatLayer
import net.casual.arcade.utils.entity.EntityTransferReason
import net.casual.arcade.utils.impl.DelayedActions
import net.casual.arcade.utils.modify
import net.casual.arcade.virtual.entity.VirtualEntity
import net.casual.arcade.virtual.entity.utils.createVirtualEntityAttachment
import net.casual.arcade.virtual.entity.utils.removeVirtualEntityAttachment
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBundlePacket
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.Pose
import org.jetbrains.annotations.ApiStatus.Experimental
import org.jetbrains.annotations.ApiStatus.Internal
import net.casual.arcade.utils.ClientboundSetPassengersPacket as createSetPassengersPacket

public class EntityNametagExtension(entity: Entity): EntityExtension(entity) {
    private val attachment = lazy { this.entity.createVirtualEntityAttachment(::NametagVirtualEntityAttachment) }

    private var mount: VirtualEntity? = null

    public fun add(nametag: Nametag) {
        this.getAttachment().attach(nametag)
    }

    public fun remove(nametag: Nametag) {
        if (this.attachment.isInitialized()) {
            this.getAttachment().detach(nametag)
        }
    }

    public fun removeAll() {
        if (this.attachment.isInitialized()) {
            this.getAttachment().detachAll()
        }
    }

    public fun all(): Collection<Nametag> {
        if (this.attachment.isInitialized()) {
            return this.getAttachment().getNametags()
        }
        return listOf()
    }

    @Experimental
    public fun setMount(mount: VirtualEntity) {
        this.mount = mount
        this.broadcastUpdatedMount()
    }

    @Experimental
    public fun removeMount() {
        this.mount = null
        this.broadcastUpdatedMount()
    }

    override fun transfer(
        entity: Entity,
        reason: EntityTransferReason,
        delayed: DelayedActions
    ): Extension {
        if (!this.attachment.isInitialized()) {
            return EntityNametagExtension(entity)
        }
        val old = this.getAttachment()
        val entities = old.getNametagEntities()
        val extension = EntityNametagExtension(entity)
        val attachment = extension.getAttachment()
        for (element in entities) {
            attachment.attach(element.nametag)
        }
        this.entity.removeVirtualEntityAttachment(old)
        return extension
    }

    @Internal
    public fun getAttachment(): NametagVirtualEntityAttachment {
        return this.attachment.value
    }

    internal fun sneak() {
        if (this.attachment.isInitialized()) {
            this.getAttachment().sneak()
        }
    }

    internal fun unsneak() {
        if (this.attachment.isInitialized()) {
            this.getAttachment().unsneak()
        }
    }

    internal fun createUpdatePassengersPacket(observer: ServerPlayer): ClientboundSetPassengersPacket {
        val mount = this.mount
        return when {
            mount == null || !mount.observers.isObserving(observer) -> ClientboundSetPassengersPacket(this.entity)
            else -> createSetPassengersPacket(mount.id, this.getRootPassengers(observer))
        }
    }

    internal fun updatePassengersPacket(
        observer: ServerPlayer,
        packet: ClientboundSetPassengersPacket
    ): ClientboundSetPassengersPacket {
        if (!this.attachment.isInitialized() || this.mount != null) {
            return packet
        }

        val updated = createSetPassengersPacket(packet.vehicle, packet.passengers + this.getRootPassengers(observer))
        return PolymerCompatLayer.updatePacket(packet, updated)
    }

    private fun getRootPassengers(observer: ServerPlayer): IntArray {
        val empty = this.getAttachment().isObservingEmpty(observer)
        return if (empty) intArrayOf() else intArrayOf(this.getAttachment().getRootId())
    }

    private fun broadcastUpdatedMount() {
        if (this.attachment.isInitialized()) {
            this.attachment.value.broadcast { observer, consumer ->
                consumer.invoke(this.createUpdatePassengersPacket(observer))
            }
        }
    }

    public companion object {
        @JvmStatic
        public val Entity.nametagExtension: EntityNametagExtension
            get() = this.getExtension()

        @Deprecated("Use nametagExtension instead")
        public fun Entity.addNametag(nametag: Nametag): Boolean {
            this.nametagExtension.add(nametag)
            return true
        }

        @Deprecated("Use nametagExtension instead")
        public fun Entity.removeNametag(nametag: Nametag): Boolean {
            this.nametagExtension.remove(nametag)
            return true
        }

        @Deprecated("Use nametagExtension instead")
        public fun Entity.getNametags(): Collection<Nametag> {
            return this.nametagExtension.all()
        }

        @Deprecated("For removal")
        public fun Entity.getNametagsElements(): Collection<NametagVirtualEntity> {
            return this.nametagExtension.getAttachment().getNametagEntities()
        }

        @Deprecated("Use nametagExtension instead")
        public fun Entity.removeNametags() {
            this.nametagExtension.removeAll()
        }

        internal fun registerEvents() {
            GlobalEventHandler.Server.register<EntityExtensionEvent> { event ->
                event.addExtension(::EntityNametagExtension)
            }
            GlobalEventHandler.Server.register<PlayerClientboundPacketEvent> { event ->
                event.replacePacket(::updatePacket)
            }
            GlobalEventHandler.Server.register<PlayerPoseEvent> { (player, previous, updated) ->
                if (previous != updated) {
                    if (previous == Pose.CROUCHING) {
                        player.nametagExtension.unsneak()
                    } else if (updated == Pose.CROUCHING) {
                        player.nametagExtension.sneak()
                    }
                }
            }
        }

        private fun updatePacket(player: ServerPlayer, packet: Packet<*>): Packet<ClientGamePacketListener> {
            if (packet is ClientboundBundlePacket) {
                return packet.modify(player, this::updatePacket)
            }
            if (packet !is ClientboundSetPassengersPacket) {
                return packet.asClientGamePacket()
            }

            val vehicle = player.level().getEntity(packet.vehicle) ?: return packet
            return vehicle.nametagExtension.updatePassengersPacket(player, packet)
        }
    }
}