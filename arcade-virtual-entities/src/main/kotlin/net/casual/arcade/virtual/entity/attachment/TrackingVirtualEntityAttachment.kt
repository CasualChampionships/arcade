/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.attachment

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.casual.arcade.virtual.entity.VirtualEntity
import net.casual.arcade.virtual.entity.utils.VirtualEntityTrackingUtils
import net.casual.arcade.virtual.entity.utils.VirtualEntityTrackingUtils.attachAndUpdateTracking
import net.casual.arcade.virtual.entity.utils.VirtualEntityTrackingUtils.detachAndUpdateTracking
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl

/**
 * A [RootVirtualEntityAttachment] implementation which keeps track
 * of the players which are currently observing it.
 *
 * @see RootVirtualEntityAttachment
 */
public abstract class TrackingVirtualEntityAttachment: RootVirtualEntityAttachment {
    private val attached = ObjectLinkedOpenHashSet<VirtualEntity>()
    protected val connections: MutableSet<ServerGamePacketListenerImpl> = ObjectOpenHashSet()

    public fun observers(): List<ServerPlayer> {
        return this.connections.map { it.player }
    }

    override fun tick() {
        VirtualEntityTrackingUtils.updateTrackedVirtualEntitiesFor(this.connections, this.attached)
        this.updateAttached()
        super.tick()
    }

    public override fun startObservingAttached(observer: ServerPlayer, quietly: Boolean) {
        if (this.connections.add(observer.connection)) {
            super.startObservingAttached(observer, quietly)
        }
    }

    public override fun stopObservingAttached(observer: ServerPlayer) {
        if (this.connections.remove(observer.connection)) {
            super.stopObservingAttached(observer)
        }
    }

    public open fun updateObservingAttached(updated: Set<ServerPlayer>) {
        val previous = this.connections.mapTo(ObjectOpenHashSet()) { connection -> connection.player }
        for (observer in (previous - updated)) {
            this.stopObservingAttached(observer)
        }
        for (observer in (updated - previous)) {
            this.startObservingAttached(observer)
        }
    }

    override fun attach(entity: VirtualEntity): Boolean {
        return this.attachAndUpdateTracking(entity, this.attached, this.connections)
    }

    override fun detach(entity: VirtualEntity): Boolean {
        return this.detachAndUpdateTracking(entity, this.attached, this.connections)
    }

    final override fun attached(): Collection<VirtualEntity> {
        return this.attached
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