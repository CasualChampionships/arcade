/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
import net.casual.arcade.virtual.entity.attachment.VirtualEntityAttachment
import net.casual.arcade.virtual.entity.attachment.anchor.AttachmentAnchor
import net.casual.arcade.virtual.entity.utils.canAttachTo
import net.casual.arcade.virtual.entity.utils.startObservingAndSendPackets
import net.casual.arcade.virtual.entity.utils.stopObservingAndSendPackets
import net.minecraft.server.level.ServerPlayer

public open class SimpleParentVirtualEntity(
    override val attachment: VirtualEntityAttachment
): TrackingVirtualEntity(), ParentVirtualEntity {
    protected val children: MutableSet<VirtualEntity> = ObjectLinkedOpenHashSet()

    override val anchor: AttachmentAnchor = super.anchor

    override fun tick() {
        for (connection in this.connections) {
            val observer = connection.player
            for (child in this.children) {
                val isObserving = child.isObserving(observer)
                val canObserve = child.canObserve(observer)
                if (!isObserving && canObserve) {
                    child.startObservingAndSendPackets(observer)
                } else if (isObserving && !canObserve) {
                    child.stopObservingAndSendPackets(observer)
                }
            }
        }

        super.tick()
    }

    override fun attach(entity: VirtualEntity): Boolean {
        return entity.canAttachTo(this) && this.children.add(entity)
    }

    override fun detach(entity: VirtualEntity): Boolean {
        if (this.children.remove(entity)) {
            this.connections.forEach { connection -> entity.stopObservingAndSendPackets(connection.player) }
            return true
        }
        return false
    }

    override fun startObserving(observer: ServerPlayer): Boolean {
        if (super<TrackingVirtualEntity>.startObserving(observer)) {
            super<ParentVirtualEntity>.startObserving(observer)
            return true
        }
        return false
    }

    override fun stopObserving(observer: ServerPlayer): Boolean {
        if (super<TrackingVirtualEntity>.stopObserving(observer)) {
            super<ParentVirtualEntity>.stopObserving(observer)
            return true
        }
        return false
    }

    override fun children(): Collection<VirtualEntity> {
        return this.children
    }
}