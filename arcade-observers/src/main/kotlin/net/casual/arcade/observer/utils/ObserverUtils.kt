/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.observer.utils

import net.casual.arcade.observer.Observer
import net.casual.arcade.observer.PlayerObserver
import net.casual.arcade.observer.extensions.PlayerObserverExtension.Companion.observerExtension
import net.minecraft.server.level.ServerPlayer

public fun Observer.asPlayerOrNull(): ServerPlayer? {
    return if (this is PlayerObserver) this.player else null
}

public fun ServerPlayer.asObserver(): PlayerObserver {
    return this.observerExtension.observer
}