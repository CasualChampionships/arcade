/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.extensions

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.extensions.PlayerExtension
import net.casual.arcade.extensions.event.PlayerExtensionEvent
import net.casual.arcade.extensions.utils.getExtension
import net.casual.arcade.virtual.entity.ParentVirtualEntity
import net.casual.arcade.virtual.entity.VirtualEntity
import net.casual.arcade.virtual.entity.attachment.VirtualEntityAttachment
import net.casual.arcade.virtual.entity.interaction.EntityInteraction
import net.casual.arcade.virtual.entity.mixins.ServerboundInteractPacketAccessor
import net.minecraft.network.protocol.game.ServerboundInteractPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.phys.Vec3

internal class PlayerAttachmentObserverExtension(player: ServerPlayer): PlayerExtension(player) {
    private val observing = ObjectOpenHashSet<VirtualEntityAttachment>()

    fun startObserving(attachment: VirtualEntityAttachment) {
        this.observing.add(attachment)
    }

    fun stopObserving(attachment: VirtualEntityAttachment) {
        this.observing.remove(attachment)
    }

    fun tryInteractWithVirtualEntity(packet: ServerboundInteractPacket): Boolean {
        val id = (packet as ServerboundInteractPacketAccessor).accessEntityId()
        val entity = this.findInteractableVirtualEntity(id) ?: return false
        val handler = entity.getInteractionHandler(this.player) ?: return false
        packet.dispatch(object: ServerboundInteractPacket.Handler {
            override fun onInteraction(hand: InteractionHand) {
                handler.interact(player, EntityInteraction.Use(hand))
            }

            override fun onInteraction(hand: InteractionHand, position: Vec3) {
                handler.interact(player, EntityInteraction.UseAt(hand, position))
            }

            override fun onAttack() {
                handler.interact(player, EntityInteraction.Attack)
            }
        })
        return true
    }

    private fun findInteractableVirtualEntity(id: Int): VirtualEntity? {
        for (attachment in this.observing) {
            if (!attachment.interactable) {
                continue
            }

            for (entity in attachment.attached()) {
                val result = this.findInteractableVirtualEntity(id, entity)
                if (result != null) {
                    return result
                }
            }
        }
        return null
    }


    private fun findInteractableVirtualEntity(id: Int, entity: VirtualEntity): VirtualEntity? {
        if (id == entity.id) {
            return entity
        }
        if (entity is ParentVirtualEntity && entity.interactable) {
            for (child in entity.children()) {
                val result = this.findInteractableVirtualEntity(id, child)
                if (result != null) {
                    return result
                }
            }
        }
        return null
    }

    companion object {
        @JvmStatic
        val ServerPlayer.attachmentObserver: PlayerAttachmentObserverExtension
            get() = this.getExtension()

        fun registerEvents() {
            GlobalEventHandler.Server.register<PlayerExtensionEvent> {
                it.addExtension(::PlayerAttachmentObserverExtension)
            }
        }
    }
}