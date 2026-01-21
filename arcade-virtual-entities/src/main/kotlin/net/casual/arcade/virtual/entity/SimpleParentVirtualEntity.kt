/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
import net.casual.arcade.virtual.entity.attachment.VirtualEntityAttachment
import net.casual.arcade.virtual.entity.utils.canAttachTo
import net.casual.arcade.virtual.entity.utils.startObservingAndSendPackets
import net.casual.arcade.virtual.entity.utils.stopObservingAndSendPackets
import net.minecraft.server.level.ServerPlayer

public open class SimpleParentVirtualEntity(
    override val attachment: VirtualEntityAttachment
): TrackingVirtualEntity(), ParentVirtualEntity {
    protected val children: MutableSet<VirtualEntity> = ObjectLinkedOpenHashSet()

    override fun attach(entity: VirtualEntity): Boolean {
        if (entity.canAttachTo(this) && this.children.add(entity)) {
            this.connections.forEach { connection -> entity.startObservingAndSendPackets(connection.player) }
            return true
        }
        return false
    }

    override fun detach(entity: VirtualEntity): Boolean {
        if (this.children.remove(entity)) {
            this.connections.forEach { connection -> entity.stopObservingAndSendPackets(connection.player) }
            return true
        }
        return false
    }

    override fun startObserving(observer: ServerPlayer) {
        super<TrackingVirtualEntity>.startObserving(observer)
        super<ParentVirtualEntity>.startObserving(observer)
    }

    override fun stopObserving(observer: ServerPlayer) {
        super<TrackingVirtualEntity>.stopObserving(observer)
        super<ParentVirtualEntity>.stopObserving(observer)
    }

    override fun children(): Collection<VirtualEntity> {
        return this.children
    }
}