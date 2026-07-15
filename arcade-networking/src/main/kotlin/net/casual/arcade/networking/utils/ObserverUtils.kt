package net.casual.arcade.networking.utils

import net.casual.arcade.networking.observer.Observer
import net.casual.arcade.networking.observer.PlayerObserver
import net.casual.arcade.networking.extensions.PlayerObserverExtension.Companion.observerExtension
import net.minecraft.server.level.ServerPlayer

public fun Observer.asPlayerOrNull(): ServerPlayer? {
    return if (this is PlayerObserver) this.player else null
}

public fun ServerPlayer.asObserver(): PlayerObserver {
    return this.observerExtension.observer
}