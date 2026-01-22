/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.attachment

import net.casual.arcade.virtual.entity.extensions.PlayerAttachmentObserverExtension.Companion.attachmentObserver
import net.casual.arcade.virtual.entity.utils.VirtualEntityPacketCollector
import net.minecraft.server.level.ServerPlayer

public interface RootVirtualEntityAttachment: VirtualEntityAttachment {
    public fun startObservingAttached(observer: ServerPlayer, quietly: Boolean = false) {
        observer.attachmentObserver.startObserving(this)
        if (quietly) {
            for (entity in this.attached()) {
                entity.startObserving(observer)
            }
            return
        }

        val collector = VirtualEntityPacketCollector()
        for (entity in this.attached()) {
            if (entity.startObserving(observer)) {
                entity.sendSpawnPackets(observer, collector::add)
            }
        }
        collector.optimize().bundle().send(observer.connection::send)
    }

    public fun stopObservingAttached(observer: ServerPlayer) {
        observer.attachmentObserver.stopObserving(this)

        val collector = VirtualEntityPacketCollector()
        for (entity in this.attached()) {
            if (entity.isObserving(observer)) {
                entity.sendDespawnPackets(observer, collector::add)
                entity.stopObserving(observer)
            }
        }
        collector.optimize().bundle().send(observer.connection::send)
    }
}