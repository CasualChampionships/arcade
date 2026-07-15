/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.attachment

import net.casual.arcade.networking.observer.Observer
import net.casual.arcade.networking.observer.PlayerObserver
import net.casual.arcade.networking.packet.PacketSender
import net.casual.arcade.virtual.entity.extensions.PlayerAttachmentObserverExtension.Companion.attachmentObserverExtension
import net.casual.arcade.virtual.entity.utils.VirtualEntityPacketCollector
import net.casual.arcade.virtual.entity.utils.sendBundledSpawnPackets
import net.casual.arcade.virtual.entity.utils.stopObservingAndSendPackets
import net.minecraft.network.protocol.Packet
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
        observer: Observer,
        quietly: Boolean = false,
        sender: PacketSender = observer
    ) {
        if (!this.observers.startObserving(observer)) {
            return
        }

        if (observer is PlayerObserver) {
            observer.player.attachmentObserverExtension.startObserving(this)
        }

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
        collector.optimize().bundle().send(sender)
    }

    @NonExtendable
    public fun stopObservingAttached(observer: Observer) {
        if (!this.observers.isObserving(observer)) {
            return
        }

        if (observer is PlayerObserver) {
            observer.player.attachmentObserverExtension.stopObserving(this)
        }

        val collector = VirtualEntityPacketCollector()
        for (entity in this.attached()) {
            entity.stopObservingAndSendPackets(observer, collector::add)
        }
        collector.optimize().bundle().send(observer)

        this.observers.stopObserving(observer)
    }

    @NonExtendable
    public fun clearObservingAttached() {
        for (observer in this.observers.toList()) {
            this.stopObservingAttached(observer)
        }
    }

    @NonExtendable
    public fun sendObservingAttachedSpawnPackets(observer: Observer, consumer: (Packet<*>) -> Unit) {
        if (this.shouldDelayObserving()) {
            return
        }

        for (entity in this.attached()) {
            if (entity.observers.isObserving(observer)) {
                entity.sendBundledSpawnPackets(observer, consumer)
            }
        }
    }

    @OverrideOnly
    public fun shouldDelayObserving(): Boolean {
        return false
    }

    @Experimental
    public fun updateObservingAttached(updated: Set<Observer>) {
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
     * @param sender The packet sender.
     */
    @Experimental
    public fun resendTo(observer: Observer, sender: PacketSender = observer)
}