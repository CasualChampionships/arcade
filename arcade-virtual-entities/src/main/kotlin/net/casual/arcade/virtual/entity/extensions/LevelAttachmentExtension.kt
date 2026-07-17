/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.extensions

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.level.LevelTickEvent
import net.casual.arcade.events.utils.register
import net.casual.arcade.extensions.event.LevelExtensionEvent
import net.casual.arcade.extensions.utils.getExtension
import net.casual.arcade.observer.Observer
import net.casual.arcade.observer.events.ObserverStartObservingLevelEvent
import net.casual.arcade.observer.events.ObserverStopObservingLevelEvent
import net.casual.arcade.observer.utils.getObservers
import net.casual.arcade.virtual.entity.attachment.RootVirtualEntityAttachment
import net.casual.arcade.virtual.entity.attachment.anchor.LevelAttachmentAnchor
import net.minecraft.server.level.ServerLevel

internal class LevelAttachmentExtension(level: ServerLevel): AttachmentExtension<LevelAttachmentAnchor> {
    override val attachments = ObjectLinkedOpenHashSet<RootVirtualEntityAttachment>()
    override val anchor = LevelAttachmentAnchor(level)

    override fun getObservers(): Collection<Observer> {
        return this.anchor.level.getObservers()
    }

    private fun startObserving(observer: Observer) {
        for (attachment in this.attachments) {
            attachment.startObservingAttached(observer)
        }
    }

    private fun stopObserving(observer: Observer) {
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
            GlobalEventHandler.Server.register<ObserverStartObservingLevelEvent> { (observer, level) ->
                level.attachmentExtension.startObserving(observer)
            }
            GlobalEventHandler.Server.register<ObserverStopObservingLevelEvent> { (observer, level) ->
                level.attachmentExtension.stopObserving(observer)
            }
        }
    }
}