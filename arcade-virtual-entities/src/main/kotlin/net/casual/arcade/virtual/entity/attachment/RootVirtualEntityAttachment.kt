/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.attachment

import net.casual.arcade.observer.Observer
import net.casual.arcade.observer.PlayerObserver
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.network.PacketSender
import net.casual.arcade.virtual.entity.extensions.PlayerObservingAttachmentsExtension.Companion.observingAttachmentsExtension
import net.casual.arcade.virtual.entity.utils.VirtualEntityPacketCollector
import net.casual.arcade.virtual.entity.utils.sendBundledSpawnPackets
import net.casual.arcade.virtual.entity.utils.stopObservingAndSendPackets
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
        this.observers.startObserving(observer)
        if (!this.observers.isObserving(observer)) {
            ArcadeUtils.logger.warn("Observer $observer failed to start observing $this")
            return
        }

        if (observer is PlayerObserver) {
            observer.player.observingAttachmentsExtension.startObserving(this)
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

        this.sendObservingAttachedSpawnPackets(observer, sender)
    }

    @NonExtendable
    public fun stopObservingAttached(observer: Observer, quietly: Boolean = false) {
        if (!this.observers.isObserving(observer)) {
            return
        }

        if (observer is PlayerObserver) {
            observer.player.observingAttachmentsExtension.stopObserving(this)
        }

        if (!quietly) {
            this.sendObservingAttachedDespawnPackets(observer, observer)
        }

        this.observers.stopObserving(observer)
    }

    @NonExtendable
    public fun clearObservingAttached() {
        for (observer in this.observers.toList()) {
            this.stopObservingAttached(observer)
        }
    }

    @NonExtendable
    public fun sendObservingAttachedSpawnPackets(observer: Observer, sender: PacketSender) {
        if (this.shouldDelayObserving()) {
            return
        }

        val collector = VirtualEntityPacketCollector()
        this.sendRootSpawnPackets(observer, collector::add)

        for (entity in this.attached()) {
            if (entity.observers.isObserving(observer)) {
                entity.sendBundledSpawnPackets(observer, collector::add)
            }
        }
        collector.optimize().bundle().send(sender)
    }

    @NonExtendable
    public fun sendObservingAttachedDespawnPackets(observer: Observer, sender: PacketSender) {
        val collector = VirtualEntityPacketCollector()
        this.sendRootDespawnPackets(observer, collector::add)
        for (entity in this.attached()) {
            entity.stopObservingAndSendPackets(observer, collector::add)
        }
        collector.optimize().bundle().send(observer)
    }

    @OverrideOnly
    public fun sendRootSpawnPackets(observer: Observer, sender: PacketSender) {

    }

    @OverrideOnly
    public fun sendRootDespawnPackets(observer: Observer, sender: PacketSender) {

    }

    @OverrideOnly
    @Experimental
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
    public fun resendSpawnPackets(observer: Observer, sender: PacketSender = observer) {
        this.sendObservingAttachedSpawnPackets(observer, sender)
    }
}