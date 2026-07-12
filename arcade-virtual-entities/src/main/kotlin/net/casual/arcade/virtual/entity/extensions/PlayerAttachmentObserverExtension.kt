/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.extensions

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.utils.register
import net.casual.arcade.extensions.PlayerExtension
import net.casual.arcade.extensions.event.PlayerExtensionEvent
import net.casual.arcade.extensions.utils.getExtension
import net.casual.arcade.virtual.entity.ParentVirtualEntity
import net.casual.arcade.virtual.entity.VirtualEntity
import net.casual.arcade.virtual.entity.attachment.RootVirtualEntityAttachment
import net.casual.arcade.virtual.entity.compat.ArcadeReplayCompatLayer
import net.casual.arcade.virtual.entity.interaction.EntityInteraction
import net.casual.arcade.virtual.entity.observer.PlayerObserver
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.phys.Vec3

internal class PlayerAttachmentObserverExtension(player: ServerPlayer): PlayerExtension(player) {
    private val observing = ObjectOpenHashSet<RootVirtualEntityAttachment>()
    val observer = PlayerObserver(player.connection)

    fun startObserving(attachment: RootVirtualEntityAttachment) {
        this.observing.add(attachment)
    }

    fun stopObserving(attachment: RootVirtualEntityAttachment) {
        this.observing.remove(attachment)
    }

    fun attachments(): Set<RootVirtualEntityAttachment> {
        return this.observing
    }

    fun tryInteractWithVirtualEntity(target: Int, hand: InteractionHand, pos: Vec3): Boolean {
        val handler = this.findInteractionHandler(target) ?: return false
        handler.interact(this.player, EntityInteraction.Use(hand, pos))
        return true
    }

    fun tryAttackVirtualEntity(target: Int): Boolean {
        val handler = this.findInteractionHandler(target) ?: return false
        handler.interact(this.player, EntityInteraction.Attack)
        return true
    }

    fun trySpectateVirtualEntity(target: Int): Boolean {
        val handler = this.findInteractionHandler(target) ?: return false
        handler.interact(this.player, EntityInteraction.Spectate)
        return true
    }

    fun tryPickVirtualEntity(target: Int, data: Boolean): Boolean {
        val handler = this.findInteractionHandler(target) ?: return false
        handler.interact(this.player, EntityInteraction.Pick(data))
        return true
    }

    private fun findInteractionHandler(id: Int): VirtualEntity.InteractionHandler? {
        val entity = this.findInteractableVirtualEntity(id) ?: return null
        return entity.getInteractionHandler(this.player)
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
        val ServerPlayer.attachmentObserverExtension: PlayerAttachmentObserverExtension
            get() = this.getExtension()

        fun registerEvents() {
            GlobalEventHandler.Server.register<PlayerExtensionEvent> {
                it.addExtension(::PlayerAttachmentObserverExtension)
            }

            if (ArcadeReplayCompatLayer.loaded) {
                ArcadeReplayCompatLayer.registerReplaySnapshotAttachmentRecording()
            }
        }
    }
}