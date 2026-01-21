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
import net.minecraft.server.level.ServerPlayer

internal class PlayerAttachmentObserverExtension(player: ServerPlayer): PlayerExtension(player) {
    private val observing = ObjectOpenHashSet<VirtualEntityAttachment>()

    fun startObserving(attachment: VirtualEntityAttachment) {
        this.observing.add(attachment)
    }

    fun stopObserving(attachment: VirtualEntityAttachment) {
        this.observing.remove(attachment)
    }

    fun findInteractableVirtualEntity(id: Int): VirtualEntity? {
        for (attachment in this.observing) {
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
        if (entity is ParentVirtualEntity && entity.canInteractWithChildren) {
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
        val ServerPlayer.attachmentObserver: PlayerAttachmentObserverExtension
            get() = this.getExtension()

        fun registerEvents() {
            GlobalEventHandler.Server.register<PlayerExtensionEvent> {
                it.addExtension(::PlayerAttachmentObserverExtension)
            }
        }
    }
}