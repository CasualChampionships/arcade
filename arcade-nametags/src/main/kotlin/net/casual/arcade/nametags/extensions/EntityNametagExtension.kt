/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.nametags.extensions

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.events.server.player.PlayerClientboundPacketEvent
import net.casual.arcade.events.server.player.PlayerClientboundPacketEvent.Companion.replacePacket
import net.casual.arcade.events.server.player.PlayerPoseEvent
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
import net.casual.arcade.virtual.entity.utils.createVirtualEntityAttachment
import net.casual.arcade.virtual.entity.utils.removeVirtualEntityAttachment
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBundlePacket
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.Pose
import org.jetbrains.annotations.ApiStatus.Internal
import net.casual.arcade.utils.ClientboundSetPassengersPacket as createSetPassengersPacket

public class EntityNametagExtension(entity: Entity): EntityExtension(entity) {
    // TODO: Only players?
    private val attachment = lazy { this.entity.createVirtualEntityAttachment(::NametagVirtualEntityAttachment) }

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

    public companion object {
        public fun Entity.addNametag(nametag: Nametag): Boolean {
            val holder = this.getExtension<EntityNametagExtension>().getAttachment()
            holder.attach(nametag)
            return true
        }

        public fun Entity.removeNametag(nametag: Nametag): Boolean {
            val holder = this.getExtension<EntityNametagExtension>().getAttachment()
            holder.detach(nametag)
            return true
        }

        public fun Entity.getNametags(): Collection<Nametag> {
            return this.getNametagsElements().map { it.nametag }
        }

        public fun Entity.getNametagsElements(): Collection<NametagVirtualEntity> {
            return this.getExtension<EntityNametagExtension>().getAttachment().getNametagEntities()
        }

        public fun Entity.removeNametags() {
            this.getExtension<EntityNametagExtension>().getAttachment().detachAll()
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
                        player.getExtension<EntityNametagExtension>().getAttachment().unsneak()
                    } else if (updated == Pose.CROUCHING) {
                        player.getExtension<EntityNametagExtension>().getAttachment().sneak()
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
            val holder = vehicle.getExtension<EntityNametagExtension>().getAttachment()
            if (holder != null) {
                val updated = createSetPassengersPacket(packet.vehicle, packet.passengers + holder.getRootId())
                return PolymerCompatLayer.updatePacket(packet, updated)
            }
            return packet
        }
    }
}