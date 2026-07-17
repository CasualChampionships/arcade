/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.observer.tracker

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
import net.casual.arcade.observer.Observer

public class SimpleObserverTracker: ObserverTracker {
    private val tracking = ObjectLinkedOpenHashSet<Observer>()

    override fun startObserving(observer: Observer): Boolean {
        return this.tracking.add(observer)
    }

    override fun stopObserving(observer: Observer) {
        this.tracking.remove(observer)
    }

    override fun isObserving(observer: Observer): Boolean {
        return this.tracking.contains(observer)
    }

    override fun observers(): Collection<Observer> {
        return this.tracking
    }
}