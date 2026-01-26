/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.tracker

import net.minecraft.network.protocol.Packet
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl

/**
 * This interface provides tracking for observers
 * of virtual entities.
 */
public interface ObserverTracker: Iterable<ServerPlayer> {
    /**
     * This function is called when an [observer]
     * wants to start observing.
     *
     * @param observer The player to start observing.
     * @return Whether the [observer] can start observing.
     */
    public fun startObserving(observer: ServerPlayer): Boolean

    /**
     * This function is called when an [observer]
     * wants to stop observing.
     *
     * @param observer The player to stop observing.
     */
    public fun stopObserving(observer: ServerPlayer)

    /**
     * Checks whether an [observer] is currently tracked as observing.
     *
     * @param observer The player to check.
     * @return Whether the [observer] is observing.
     */
    public fun isObserving(observer: ServerPlayer): Boolean

    /**
     * Gets all the connections of all the players currently observing.
     *
     * @return All the connections.
     */
    public fun connections(): Collection<ServerGamePacketListenerImpl>

    /**
     * Broadcasts a packet to all observers.
     *
     * @param packet The packet to broadcast.
     */
    public fun broadcast(packet: Packet<*>) {
        for (connection in this.connections()) {
            connection.send(packet)
        }
    }

    override fun iterator(): Iterator<ServerPlayer> {
        return this.connections().map(ServerGamePacketListenerImpl::player).iterator()
    }
}