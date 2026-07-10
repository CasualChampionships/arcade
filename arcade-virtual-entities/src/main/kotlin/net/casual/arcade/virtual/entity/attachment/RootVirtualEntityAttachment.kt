/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.attachment

import net.casual.arcade.virtual.entity.extensions.PlayerAttachmentObserverExtension.Companion.attachmentObserver
import net.casual.arcade.virtual.entity.utils.VirtualEntityPacketCollector
import net.casual.arcade.virtual.entity.utils.stopObservingAndSendPackets
import net.minecraft.network.protocol.Packet
import net.minecraft.server.level.ServerPlayer
import org.jetbrains.annotations.ApiStatus.*

/**
 * This interface is the root of all [VirtualEntityAttachment]s.
 *
 * This interface provides [startObservingAttached] and [stopObservingAttached]
 * allowing observers to observe all the attached entities.
 */
public interface RootVirtualEntityAttachment: VirtualEntityAttachment {
    @NonExtendable
    public fun startObservingAttached(
        observer: ServerPlayer,
        quietly: Boolean = false,
        consumer: (Packet<*>) -> Unit = observer.connection::send
    ) {
        if (!this.observers.startObserving(observer)) {
            return
        }

        observer.attachmentObserver.startObserving(this)

        if (this.shouldDelayObserving()) {
            return
        }

        for (entity in this.attached()) {
            if (entity.canObserve(observer)) {
                entity.observers.startObserving(observer)
            }
        }
        if (quietly) {
            return
        }

        val collector = VirtualEntityPacketCollector()
        this.sendObservingAttachedSpawnPackets(observer, collector::add)
        collector.optimize().bundle().send(consumer)
    }

    @NonExtendable
    public fun stopObservingAttached(observer: ServerPlayer) {
        if (!this.observers.isObserving(observer)) {
            return
        }

        observer.attachmentObserver.stopObserving(this)

        val collector = VirtualEntityPacketCollector()
        for (entity in this.attached()) {
            entity.stopObservingAndSendPackets(observer, collector::add)
        }
        collector.optimize().bundle().send(observer.connection::send)

        this.observers.stopObserving(observer)
    }

    @NonExtendable
    public fun sendObservingAttachedSpawnPackets(observer: ServerPlayer, consumer: (Packet<*>) -> Unit) {
        if (this.shouldDelayObserving()) {
            return
        }

        for (entity in this.attached()) {
            if (entity.observers.isObserving(observer)) {
                entity.sendSpawnPackets(observer, consumer)
            }
        }
    }

    @OverrideOnly
    public fun shouldDelayObserving(): Boolean {
        return false
    }

    @Experimental
    public fun updateObservingAttached(updated: Set<ServerPlayer>) {
        val previous = this.observers.toSet()
        for (observer in (previous - updated)) {
            this.stopObservingAttached(observer)
        }
        for (observer in (updated - previous)) {
            this.startObservingAttached(observer)
        }
    }

    /**
     * This resends all packets for all virtual entities
     * attached to this attachment.
     *
     * @param observer The player to resend to.
     * @param consumer The packet consumer.
     */
    @Experimental
    public fun resendTo(observer: ServerPlayer, consumer: (Packet<*>) -> Unit)
}