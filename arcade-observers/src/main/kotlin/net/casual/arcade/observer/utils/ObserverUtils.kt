/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.observer.utils

import net.casual.arcade.observer.Observer
import net.casual.arcade.observer.PlayerObserver
import net.casual.arcade.observer.extensions.EntityObserversExtension.Companion.observersExtension
import net.casual.arcade.observer.extensions.LevelObserversExtension.Companion.observersExtension
import net.casual.arcade.observer.extensions.PlayerObserverExtension.Companion.observerExtension
import net.casual.arcade.observer.tracker.ObserverTracker
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import org.jetbrains.annotations.ApiStatus.Internal

public fun Observer.asPlayerOrNull(): ServerPlayer? {
    return if (this is PlayerObserver) this.player else null
}

public fun ServerPlayer.asObserver(): PlayerObserver {
    return this.observerExtension.observer
}

public fun Entity.getObservers(): ObserverTracker {
    return this.observersExtension.getObservers()
}

public fun ServerLevel.getObservers(): ObserverTracker {
    return this.observersExtension.getObservers()
}

public fun Observer.startObserving(entity: Entity) {
    entity.observersExtension.startObserving(this)
}

public fun Observer.stopObserving(entity: Entity) {
    entity.observersExtension.stopObserving(this)
}

public fun Observer.startObserving(level: ServerLevel) {
    level.observersExtension.startObserving(this)
}

public fun Observer.stopObserving(level: ServerLevel) {
    level.observersExtension.stopObserving(this)
}