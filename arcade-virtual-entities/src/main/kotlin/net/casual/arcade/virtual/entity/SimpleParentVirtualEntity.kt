/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
import net.casual.arcade.virtual.entity.attachment.VirtualEntityAttachment
import net.casual.arcade.virtual.entity.attachment.anchor.AttachmentAnchor
import net.casual.arcade.virtual.entity.utils.VirtualEntityTrackingUtils
import net.casual.arcade.virtual.entity.utils.VirtualEntityTrackingUtils.attachAndUpdateTracking
import net.casual.arcade.virtual.entity.utils.VirtualEntityTrackingUtils.detachAndUpdateTracking
import net.minecraft.server.level.ServerPlayer

/**
 * Simple implementation of [ParentVirtualEntity].
 */
public open class SimpleParentVirtualEntity(
    override val attachment: VirtualEntityAttachment
): TrackingVirtualEntity(), ParentVirtualEntity {
    protected val children: MutableSet<VirtualEntity> = ObjectLinkedOpenHashSet()

    override val anchor: AttachmentAnchor = super.anchor

    override fun tick() {
        VirtualEntityTrackingUtils.updateTrackedVirtualEntitiesFor(this.connections, this.children)
        this.updateChildren()
        super.tick()
    }

    override fun attach(entity: VirtualEntity): Boolean {
        return this.attachAndUpdateTracking(entity, this.children, this.connections)
    }

    override fun detach(entity: VirtualEntity): Boolean {
        return this.detachAndUpdateTracking(entity, this.children, this.connections)
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

    /**
     * This method should be overridden if you
     * want to update the [children] elements
     * every tick.
     *
     * This method gets called *after* observers
     * have been updated but *before* changes have been
     * broadcasted to observers.
     */
    protected open fun updateChildren() {

    }
}