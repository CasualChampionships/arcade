/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity

import net.casual.arcade.utils.math.location.LocationWithLevel
import net.casual.arcade.virtual.entity.attachment.VirtualEntityAttachment
import net.minecraft.network.protocol.Packet
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer

public interface ParentVirtualEntity: VirtualEntity, VirtualEntityAttachment {
    public val canInteractWithChildren: Boolean
        get() = false

    override val origin: LocationWithLevel<ServerLevel>
        get() = this.attachment.origin

    public fun children(): Collection<VirtualEntity>

    override fun attached(): Collection<VirtualEntity> {
        return this.children()
    }

    override fun tick() {
        for (child in this.children()) {
            child.tick()
        }
    }

    override fun startObserving(observer: ServerPlayer) {
        for (child in this.children()) {
            child.startObserving(observer)
        }
    }

    override fun stopObserving(observer: ServerPlayer) {
        for (child in this.children()) {
            child.stopObserving(observer)
        }
    }

    override fun sendSpawnPackets(observer: ServerPlayer, consumer: (Packet<*>) -> Unit) {
        for (child in this.children()) {
            child.sendSpawnPackets(observer, consumer)
        }
    }

    override fun sendDespawnPackets(observer: ServerPlayer, consumer: (Packet<*>) -> Unit) {
        for (child in this.children()) {
            child.sendDespawnPackets(observer, consumer)
        }
    }
}