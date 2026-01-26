/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.utils

import net.casual.arcade.virtual.entity.VirtualEntity
import net.casual.arcade.virtual.entity.attachment.VirtualEntityAttachment
import net.minecraft.server.network.ServerGamePacketListenerImpl

public object VirtualEntityTrackingUtils {
    public fun VirtualEntityAttachment.attachAndUpdateTracking(
        entity: VirtualEntity,
        connections: Iterable<ServerGamePacketListenerImpl>,
        tracked: MutableCollection<VirtualEntity>
    ): Boolean {
        if (entity.canAttachTo(this) && tracked.add(entity)) {
            connections.forEach { connection -> entity.startObservingAndSendPackets(connection.player) }
            return true
        }
        return false
    }

    @Suppress("UnusedReceiverParameter")
    public fun VirtualEntityAttachment.detachAndUpdateTracking(
        entity: VirtualEntity,
        connections: Iterable<ServerGamePacketListenerImpl>,
        tracked: MutableCollection<VirtualEntity>
    ): Boolean {
        if (tracked.remove(entity)) {
            connections.forEach { connection -> entity.stopObservingAndSendPackets(connection.player) }
            return true
        }
        return false
    }

    public fun updateTrackedVirtualEntitiesFor(
        connections: Iterable<ServerGamePacketListenerImpl>,
        entities: Iterable<VirtualEntity>
    ) {
        for (connection in connections) {
            val observer = connection.player
            for (entity in entities) {
                if (!entity.observers.isObserving(observer)) {
                    entity.startObservingAndSendPackets(observer)
                } else if (!entity.canObserve(observer)) {
                    entity.stopObservingAndSendPackets(observer)
                }
            }
        }
    }
}