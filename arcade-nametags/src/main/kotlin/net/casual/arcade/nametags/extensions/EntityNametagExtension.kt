/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.nametags.extensions

import eu.pb4.polymer.core.impl.interfaces.EntityAttachedPacket
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.events.server.player.PlayerClientboundPacketEvent
import net.casual.arcade.events.server.player.PlayerClientboundPacketEvent.Companion.replacePacket
import net.casual.arcade.events.server.player.PlayerPoseEvent
import net.casual.arcade.extensions.EntityExtension
import net.casual.arcade.extensions.Extension
import net.casual.arcade.extensions.TransferableEntityExtension
import net.casual.arcade.extensions.event.EntityExtensionEvent
import net.casual.arcade.extensions.event.EntityExtensionEvent.Companion.getExtension
import net.casual.arcade.nametags.ArcadeNametags
import net.casual.arcade.nametags.Nametag
import net.casual.arcade.nametags.virtual.NametagElement
import net.casual.arcade.nametags.virtual.NametagElementHolder
import net.casual.arcade.utils.asClientGamePacket
import net.casual.arcade.utils.modify
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBundlePacket
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.Pose

public class EntityNametagExtension(entity: Entity): EntityExtension(entity) {
    private val attachment: EntityAttachment?

    init {
        val holder = ArcadeNametags.createNametagElementHolder(entity)
        if (holder != null) {
            this.attachment = EntityAttachment.ofTicking(holder, entity)
        } else {
            this.attachment = null
        }
    }

    override fun transfer(entity: Entity, reason: TransferableEntityExtension.TransferReason): Extension {
        val old = this.attachment ?: return EntityNametagExtension(entity)
        val elements = (old.holder() as? NametagElementHolder)?.getNametagElements() ?: listOf()
        val extension = EntityNametagExtension(entity)
        val holder = extension.getHolder()
        if (holder != null) {
            for (element in elements) {
                holder.add(element.nametag)
            }
        }
        old.destroy()
        return extension
    }

    private fun getHolder(): NametagElementHolder? {
        return this.attachment?.holder() as? NametagElementHolder
    }

    public companion object {
        public fun Entity.addNametag(nametag: Nametag): Boolean {
            val holder = this.getExtension<EntityNametagExtension>().getHolder() ?: return false
            holder.add(nametag)
            return true
        }

        public fun Entity.removeNametag(nametag: Nametag): Boolean {
            val holder = this.getExtension<EntityNametagExtension>().getHolder() ?: return false
            holder.remove(nametag)
            return true
        }

        public fun Entity.getNametags(): Collection<Nametag> {
            return this.getNametagsElements().map { it.nametag }
        }

        public fun Entity.getNametagsElements(): Collection<NametagElement> {
            return this.getExtension<EntityNametagExtension>().getHolder()?.getNametagElements() ?: listOf()
        }

        public fun Entity.removeNametags() {
            this.getExtension<EntityNametagExtension>().getHolder()?.removeAll()
        }

        internal  fun registerEvents() {
            GlobalEventHandler.Server.register<EntityExtensionEvent> { event ->
                event.addExtension(::EntityNametagExtension)
            }
            GlobalEventHandler.Server.register<PlayerClientboundPacketEvent> { event ->
                event.replacePacket(::updatePacket)
            }
            GlobalEventHandler.Server.register<PlayerPoseEvent> { (player, previous, updated) ->
                if (previous != updated) {
                    if (previous == Pose.CROUCHING) {
                        player.getExtension<EntityNametagExtension>().getHolder()?.unsneak()
                    } else if (updated == Pose.CROUCHING) {
                        player.getExtension<EntityNametagExtension>().getHolder()?.sneak()
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
            val holder = vehicle.getExtension<EntityNametagExtension>().getHolder()
            if (holder != null && holder.isMountedToOwner()) {
                val updated = VirtualEntityUtils.createRidePacket(packet.vehicle, packet.passengers + holder.root.id)
                EntityAttachedPacket.setIfEmpty(updated, EntityAttachedPacket.get(updated))
                return updated
            }
            return packet
        }
    }
}