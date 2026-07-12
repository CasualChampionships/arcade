/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.observer.tracker

import net.casual.arcade.virtual.entity.observer.Observer
import net.minecraft.network.protocol.Packet

/**
 * This interface provides tracking for observers
 * of virtual entities.
 */
public interface ObserverTracker: Iterable<Observer> {
    /**
     * This function is called when an [observer]
     * wants to start observing.
     *
     * @param observer The player to start observing.
     * @return Whether the [observer] can start observing.
     */
    public fun startObserving(observer: Observer): Boolean

    /**
     * This function is called when an [observer]
     * wants to stop observing.
     *
     * @param observer The player to stop observing.
     */
    public fun stopObserving(observer: Observer)

    /**
     * Checks whether an [observer] is currently tracked as observing.
     *
     * @param observer The player to check.
     * @return Whether the [observer] is observing.
     */
    public fun isObserving(observer: Observer): Boolean

    /**
     * Gets all the connections of all the players currently observing.
     *
     * @return All the connections.
     */
    public fun observers(): Collection<Observer>

    /**
     * Broadcasts a packet to all observers.
     *
     * @param packet The packet to broadcast.
     */
    public fun broadcast(packet: Packet<*>) {
        this.broadcast { observer -> observer.send(packet) }
    }

    /**
     * Broadcasts packets to players using the provided sender lambda.
     *
     * @param sender The sender lambda.
     */
    public fun broadcast(sender: (Observer) -> Unit) {
        for (observer in this.observers().toList()) {
            sender.invoke(observer)
        }
    }

    override fun iterator(): Iterator<Observer> {
        return this.observers().iterator()
    }
}