/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.extensions

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.entity.EntityStartTrackingEvent
import net.casual.arcade.events.server.entity.EntityStopTrackingEvent
import net.casual.arcade.events.server.level.LevelTickEvent
import net.casual.arcade.events.utils.register
import net.casual.arcade.extensions.Extension
import net.casual.arcade.extensions.event.LevelExtensionEvent
import net.casual.arcade.extensions.utils.getExtension
import net.casual.arcade.virtual.entity.VirtualEntity
import net.casual.arcade.virtual.entity.attachment.RootVirtualEntityAttachment
import net.casual.arcade.virtual.entity.attachment.anchor.LevelAttachmentAnchor
import net.casual.arcade.virtual.entity.observer.Observer
import net.casual.arcade.virtual.entity.utils.asObserver
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer

internal class LevelAttachmentExtension(level: ServerLevel): Extension {
    private val attachments = ObjectLinkedOpenHashSet<RootVirtualEntityAttachment>()
    private val anchor = LevelAttachmentAnchor(level)

    fun tick() {
        for (attachment in this.attachments) {
            attachment.tick()
        }
    }

    fun <T: RootVirtualEntityAttachment> add(factory: (LevelAttachmentAnchor) -> T): T {
        val attachment = factory.invoke(this.anchor)
        require(attachment.anchor === this.anchor) { "Created VirtualEntityAttachment with incorrect anchor!" }
        this.attachments.add(attachment)
        for (player in this.anchor.level.players()) {
            attachment.startObservingAttached(player.asObserver())
        }
        return attachment
    }

    fun remove(attachment: RootVirtualEntityAttachment): Boolean {
        if (this.attachments.remove(attachment)) {
            attachment.clearObservingAttached()
            return true
        }
        return false
    }

    fun getAttachedVirtualEntities(): List<VirtualEntity> {
        return this.attachments.flatMap { it.attached() }
    }

    fun startObserving(observer: Observer) {
        for (attachment in this.attachments) {
            attachment.startObservingAttached(observer)
        }
    }

    fun stopObserving(observer: Observer) {
        for (attachment in this.attachments) {
            attachment.stopObservingAttached(observer)
        }
    }

    companion object {
        @JvmStatic
        val ServerLevel.attachmentExtension: LevelAttachmentExtension
            get() = this.getExtension()

        fun registerEvents() {
            GlobalEventHandler.Server.register<LevelExtensionEvent> {
                it.addExtension(::LevelAttachmentExtension)
            }
            GlobalEventHandler.Server.register<LevelTickEvent> { (level) ->
                level.attachmentExtension.tick()
            }
            GlobalEventHandler.Server.register<EntityStartTrackingEvent>(phase = EntityStartTrackingEvent.PHASE_POST) { (entity, level) ->
                if (entity is ServerPlayer) {
                    level.attachmentExtension.startObserving(entity.asObserver())
                }
            }
            GlobalEventHandler.Server.register<EntityStopTrackingEvent>(phase = EntityStartTrackingEvent.PHASE_PRE) { (entity, level) ->
                if (entity is ServerPlayer) {
                    level.attachmentExtension.stopObserving(entity.asObserver())
                }
            }
        }
    }
}