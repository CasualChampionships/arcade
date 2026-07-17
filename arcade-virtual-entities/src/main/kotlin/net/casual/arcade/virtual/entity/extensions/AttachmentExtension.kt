/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.extensions

import net.casual.arcade.extensions.Extension
import net.casual.arcade.observer.Observer
import net.casual.arcade.virtual.entity.VirtualEntity
import net.casual.arcade.virtual.entity.attachment.RootVirtualEntityAttachment
import net.casual.arcade.virtual.entity.attachment.anchor.AttachmentAnchor
import org.jetbrains.annotations.ApiStatus.NonExtendable

internal interface AttachmentExtension<A: AttachmentAnchor>: Extension {
    val attachments: MutableCollection<RootVirtualEntityAttachment>
    val anchor: A

    fun getObservers(): Collection<Observer>

    @NonExtendable
    fun tick() {
        for (attachment in this.attachments) {
            attachment.tick()
        }
    }

    @NonExtendable
    fun <T: RootVirtualEntityAttachment> add(factory: (A) -> T): T {
        val attachment = factory.invoke(this.anchor)
        require(attachment.anchor === this.anchor) { "Created VirtualEntityAttachment with incorrect anchor!" }
        require(!this.attachments.contains(attachment)) { "Created VirtualEntityAttachment was already attached!" }
        this.attachments.add(attachment)
        for (observer in this.getObservers()) {
            attachment.startObservingAttached(observer)
        }
        return attachment
    }

    @NonExtendable
    fun remove(attachment: RootVirtualEntityAttachment): Boolean {
        if (this.attachments.remove(attachment)) {
            attachment.clearObservingAttached()
            return true
        }
        return false
    }

    @NonExtendable
    fun getAttachedVirtualEntities(): List<VirtualEntity> {
        return if (this.attachments.isEmpty()) listOf() else this.attachments.flatMap { it.attached() }
    }
}