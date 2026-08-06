/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.visuals.utils

import net.casual.arcade.observer.Observer
import net.casual.arcade.utils.arcade
import net.casual.arcade.utils.network.PacketSender
import net.casual.arcade.virtual.visuals.VirtualVisual
import org.jetbrains.annotations.ApiStatus.Internal

private val OBSERVING_VISUALS = Observer.Context.Key<LinkedHashSet<VirtualVisual>>(arcade("observing_virtual_visuals"))

public fun VirtualVisual.startObservingAndSendPackets(observer: Observer, sender: PacketSender = observer) {
    if (this.observers.startObserving(observer)) {
        observer.context.getOrSet(OBSERVING_VISUALS, ::LinkedHashSet).add(this)
        this.onStartObserving(observer)
        this.sendSpawnPackets(observer, sender)
    }
}

public fun VirtualVisual.stopObservingAndSendPackets(observer: Observer, sender: PacketSender = observer) {
    if (this.observers.isObserving(observer)) {
        this.onStopObserving(observer)
        this.sendDespawnPackets(observer, sender)
        this.observers.stopObserving(observer)
        observer.context.get(OBSERVING_VISUALS)?.remove(this)
    }
}

/**
 * Gets all the [VirtualVisual]s this observer is currently observing.
 *
 * @return The visuals being observed.
 */
public fun Observer.observingVisuals(): Collection<VirtualVisual> {
    return this.context.get(OBSERVING_VISUALS) ?: listOf()
}

public fun Observer.stopObservingVisuals() {
    val visuals = this.context.get(OBSERVING_VISUALS) ?: return
    for (visual in visuals.toList()) {
        visual.stopObservingQuietly(this)
    }
    this.context.remove(OBSERVING_VISUALS)
}

@Internal
public fun VirtualVisual.stopObservingQuietly(observer: Observer) {
    if (this.observers.isObserving(observer)) {
        this.onStopObserving(observer)
        this.observers.stopObserving(observer)
        observer.context.get(OBSERVING_VISUALS)?.remove(this)
    }
}
