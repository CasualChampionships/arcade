/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.visuals

import net.casual.arcade.observer.Observer
import net.casual.arcade.observer.tracker.ObserverTracker
import net.casual.arcade.utils.network.PacketSender

public interface VirtualVisual {
    public val observers: ObserverTracker

    public fun sendSpawnPackets(observer: Observer, sender: PacketSender)

    public fun sendDespawnPackets(observer: Observer, sender: PacketSender)
}