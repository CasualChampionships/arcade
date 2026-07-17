/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.observer.tracker

import net.casual.arcade.observer.Observer

public class ParentObserverTracker(
    private val parent: ObserverTracker
): ObserverTracker {
    override fun startObserving(observer: Observer): Boolean {
        return false
    }

    override fun stopObserving(observer: Observer) {

    }

    override fun isObserving(observer: Observer): Boolean {
        return this.parent.isObserving(observer)
    }

    override fun observers(): Collection<Observer> {
        return this.parent.observers()
    }
}