/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.visuals.utils

import net.casual.arcade.observer.Observer
import net.casual.arcade.utils.network.PacketSender
import net.casual.arcade.virtual.visuals.VirtualVisual

public fun VirtualVisual.startObservingAndSendPackets(observer: Observer, sender: PacketSender = observer) {
    if (this.observers.startObserving(observer)) {
        this.sendSpawnPackets(observer, sender)
    }
}

public fun VirtualVisual.stopObservingAndSendPackets(observer: Observer, sender: PacketSender = observer) {
    if (this.observers.isObserving(observer)) {
        this.sendDespawnPackets(observer, sender)
        this.observers.stopObserving(observer)
    }
}