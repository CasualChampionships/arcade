/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity

import net.casual.arcade.virtual.entity.attachment.VirtualEntityAttachment
import net.casual.arcade.virtual.entity.attachment.anchor.AttachmentAnchor
import net.casual.arcade.virtual.entity.attachment.anchor.ParentAttachmentAnchor
import net.minecraft.network.protocol.Packet
import net.minecraft.server.level.ServerPlayer

/**
 * This interface represents a [VirtualEntity] which can
 * have children [VirtualEntity]s attached to it.
 *
 * @see VirtualEntity
 * @see VirtualEntityAttachment
 */
public interface ParentVirtualEntity: VirtualEntity, VirtualEntityAttachment {
    /**
     * Whether the children should be searched for
     * interaction handlers or not.
     */
    public val canInteractWithChildren: Boolean
        get() = false

    override val anchor: AttachmentAnchor
        get() = ParentAttachmentAnchor(this)

    /**
     * Gets all the children virtual entities.
     *
     * @return The children virtual entities.
     */
    public fun children(): Collection<VirtualEntity>

    @Deprecated("Call ParentVirtualEntity.children() instead")
    override fun attached(): Collection<VirtualEntity> {
        return this.children()
    }

    override fun tick() {
        for (child in this.children()) {
            child.tick()
        }
    }

    override fun sendSpawnPackets(observer: ServerPlayer, consumer: (Packet<*>) -> Unit) {
        for (child in this.children()) {
            if (child.observers.isObserving(observer)) {
                child.sendSpawnPackets(observer, consumer)
            }
        }
    }

    override fun sendDespawnPackets(observer: ServerPlayer, consumer: (Packet<*>) -> Unit) {
        for (child in this.children()) {
            if (child.observers.isObserving(observer)) {
                child.sendDespawnPackets(observer, consumer)
            }
        }
    }
}