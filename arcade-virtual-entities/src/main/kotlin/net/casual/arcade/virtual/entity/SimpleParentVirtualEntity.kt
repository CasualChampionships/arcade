/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
import net.casual.arcade.virtual.entity.attachment.VirtualEntityAttachment
import net.casual.arcade.virtual.entity.attachment.anchor.AttachmentAnchor
import net.casual.arcade.virtual.entity.location.VirtualPosition
import net.casual.arcade.virtual.entity.location.VirtualRotation
import net.casual.arcade.virtual.entity.observer.tracker.ObserverTracker
import net.casual.arcade.virtual.entity.observer.tracker.SimpleObserverTracker
import net.casual.arcade.virtual.entity.utils.VirtualEntityTrackingUtils
import net.casual.arcade.virtual.entity.utils.VirtualEntityTrackingUtils.attachAndUpdateTracking
import net.casual.arcade.virtual.entity.utils.VirtualEntityTrackingUtils.detachAndUpdateTracking
import java.util.*

/**
 * Simple implementation of [ParentVirtualEntity].
 */
public open class SimpleParentVirtualEntity(
    override val attachment: VirtualEntityAttachment,
    override val observers: ObserverTracker = SimpleObserverTracker()
): VirtualEntity, ParentVirtualEntity {
    override val id: Int = VirtualEntity.getNextEntityId()
    override val uuid: UUID = UUID.randomUUID()
    override var position: VirtualPosition = VirtualPosition.DEFAULT
    override var rotation: VirtualRotation = VirtualRotation.DEFAULT

    protected val children: MutableSet<VirtualEntity> = ObjectLinkedOpenHashSet()

    override val anchor: AttachmentAnchor = super.anchor
    override var interactable: Boolean = super.interactable

    override fun tick() {
        VirtualEntityTrackingUtils.updateTrackedVirtualEntitiesFor(this.observers, this.children)
        this.updateChildren()
        super.tick()
    }

    override fun attach(entity: VirtualEntity): Boolean {
        return this.attachAndUpdateTracking(entity, this.observers, this.children)
    }

    override fun detach(entity: VirtualEntity): Boolean {
        return this.detachAndUpdateTracking(entity, this.observers, this.children)
    }

    override fun children(): Iterable<VirtualEntity> {
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