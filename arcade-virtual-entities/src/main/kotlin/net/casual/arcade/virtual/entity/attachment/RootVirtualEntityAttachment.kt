/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.attachment

import net.casual.arcade.virtual.entity.extensions.PlayerAttachmentObserverExtension.Companion.attachmentObserver
import net.casual.arcade.virtual.entity.utils.VirtualEntityPacketCollector
import net.casual.arcade.virtual.entity.utils.stopObservingAndSendPackets
import net.minecraft.server.level.ServerPlayer
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.annotations.ApiStatus.NonExtendable

/**
 * This interface is the root of all [VirtualEntityAttachment]s.
 *
 * This interface provides [startObservingAttached] and [stopObservingAttached]
 * allowing observers to observe all the attached entities.
 */
public interface RootVirtualEntityAttachment: VirtualEntityAttachment {
    @NonExtendable
    public fun startObservingAttached(observer: ServerPlayer, quietly: Boolean = false) {
        if (!this.observers.startObserving(observer)) {
            return
        }

        observer.attachmentObserver.startObserving(this)
        if (quietly) {
            for (entity in this.attached()) {
                entity.observers.startObserving(observer)
            }
            return
        }

        val collector = VirtualEntityPacketCollector()
        for (entity in this.attached()) {
            if (entity.observers.startObserving(observer)) {
                entity.sendSpawnPackets(observer, collector::add)
            }
        }
        collector.bundle().send(observer.connection::send)
    }

    @NonExtendable
    public fun stopObservingAttached(observer: ServerPlayer) {
        if (!this.observers.isObserving(observer)) {
            return
        }

        observer.attachmentObserver.stopObserving(this)

        val collector = VirtualEntityPacketCollector()
        for (entity in this.attached()) {
            if (entity.observers.isObserving(observer)) {
                entity.sendDespawnPackets(observer, collector::add)
                entity.observers.stopObserving(observer)
            }
        }
        collector.optimize().bundle().send(observer.connection::send)

        this.observers.stopObserving(observer)
    }

    public fun updateObservingAttached(updated: Set<ServerPlayer>) {
        val previous = this.observers.toSet()
        for (observer in (previous - updated)) {
            this.stopObservingAttached(observer)
        }
        for (observer in (updated - previous)) {
            this.startObservingAttached(observer)
        }
    }
}