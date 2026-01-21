/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.attachment

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.casual.arcade.virtual.entity.VirtualEntity
import net.casual.arcade.virtual.entity.utils.*
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl

public abstract class TrackedVirtualEntityAttachment: VirtualEntityAttachment {
    private val attached = ObjectLinkedOpenHashSet<VirtualEntity>()
    protected val connections: MutableSet<ServerGamePacketListenerImpl> = ObjectOpenHashSet()

    public fun observers(): List<ServerPlayer> {
        return this.connections.map { it.player }
    }

    public fun startObserving(observer: ServerPlayer) {
        if (this.connections.add(observer.connection)) {
            this.startObservingAttachedFor(observer)
        }
    }

    public fun stopObserving(observer: ServerPlayer) {
        if (this.connections.remove(observer.connection)) {
            this.stopObservingAttachedFor(observer)
        }
    }

    override fun attach(entity: VirtualEntity): Boolean {
        if (entity.canAttachTo(this) && this.attached.add(entity)) {
            this.connections.forEach { connection -> entity.startObservingAndSendPackets(connection.player) }
            return true
        }
        return false
    }

    override fun detach(entity: VirtualEntity): Boolean {
        if (this.attached.remove(entity)) {
            this.connections.forEach { connection -> entity.stopObservingAndSendPackets(connection.player) }
            return true
        }
        return false
    }

    override fun attached(): Collection<VirtualEntity> {
        return this.attached
    }

    protected fun updateObservers(updated: Set<ServerPlayer>) {
        val previous = this.connections.mapTo(ObjectOpenHashSet()) { connection -> connection.player }
        for (observer in (previous - updated)) {
            this.stopObserving(observer)
        }
        for (observer in (updated - previous)) {
            this.startObserving(observer)
        }
    }
}