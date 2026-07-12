/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity

import net.casual.arcade.virtual.entity.attachment.VirtualEntityAttachment
import net.casual.arcade.virtual.entity.attachment.anchor.AttachmentAnchor
import net.casual.arcade.virtual.entity.attachment.anchor.ParentAttachmentAnchor
import net.casual.arcade.virtual.entity.observer.Observer
import net.casual.arcade.virtual.entity.observer.PacketSender

/**
 * This interface represents a [VirtualEntity] which can
 * have children [VirtualEntity]s attached to it.
 *
 * @see VirtualEntity
 * @see VirtualEntityAttachment
 */
public interface ParentVirtualEntity: VirtualEntity, VirtualEntityAttachment {
    override val anchor: AttachmentAnchor
        get() = ParentAttachmentAnchor(this)

    /**
     * Gets all the children virtual entities.
     *
     * @return The children virtual entities.
     */
    public fun children(): Iterable<VirtualEntity>

    @Deprecated("Call ParentVirtualEntity.children() instead")
    override fun attached(): Iterable<VirtualEntity> {
        return this.children()
    }

    override fun tick() {
        for (child in this.children()) {
            child.tick()
        }
    }

    override fun sendSpawnPackets(observer: Observer, sender: PacketSender) {
        for (child in this.children()) {
            if (child.observers.isObserving(observer)) {
                child.sendSpawnPackets(observer, sender)
            }
        }
    }

    override fun sendDespawnPackets(observer: Observer, sender: PacketSender) {
        for (child in this.children()) {
            if (child.observers.isObserving(observer)) {
                child.sendDespawnPackets(observer, sender)
            }
        }
    }
}