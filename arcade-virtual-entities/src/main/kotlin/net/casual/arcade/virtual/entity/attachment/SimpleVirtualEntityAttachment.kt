/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.attachment

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
import net.casual.arcade.observer.Observer
import net.casual.arcade.observer.tracker.ObserverTracker
import net.casual.arcade.observer.tracker.SimpleObserverTracker
import net.casual.arcade.utils.network.PacketSender
import net.casual.arcade.virtual.entity.VirtualEntity
import net.casual.arcade.virtual.entity.attachment.anchor.AttachmentAnchor
import net.casual.arcade.virtual.entity.utils.VirtualEntityTrackingUtils
import net.casual.arcade.virtual.entity.utils.VirtualEntityTrackingUtils.attachAndUpdateTracking
import net.casual.arcade.virtual.entity.utils.VirtualEntityTrackingUtils.detachAndUpdateTracking
import net.casual.arcade.virtual.entity.utils.sendBundledSpawnPackets

/**
 * Simple implementation of [VirtualEntityAttachment].
 */
public open class SimpleVirtualEntityAttachment(
    override val anchor: AttachmentAnchor,
    override val observers: ObserverTracker = SimpleObserverTracker()
): RootVirtualEntityAttachment {
    private val attached = ObjectLinkedOpenHashSet<VirtualEntity>()

    override var interactable: Boolean = super.interactable

    override fun tick() {
        VirtualEntityTrackingUtils.updateTrackedVirtualEntitiesFor(this.observers, this.attached)
        this.updateAttached()
        super.tick()
    }

    override fun attach(entity: VirtualEntity): Boolean {
        return this.attachAndUpdateTracking(entity, this.observers, this.attached)
    }

    override fun detach(entity: VirtualEntity): Boolean {
        return this.detachAndUpdateTracking(entity, this.observers, this.attached)
    }

    final override fun attached(): Collection<VirtualEntity> {
        return this.attached
    }

    override fun resendTo(observer: Observer, sender: PacketSender) {
        for (entity in this.attached()) {
            if (entity.observers.isObserving(observer)) {
                entity.sendBundledSpawnPackets(observer, sender)
            }
        }
    }

    /**
     * This method should be overridden if you
     * want to update the [attached] elements
     * every tick.
     *
     * This method gets called *after* observers
     * have been updated but *before* changes have been
     * broadcasted to observers.
     */
    protected open fun updateAttached() {

    }
}