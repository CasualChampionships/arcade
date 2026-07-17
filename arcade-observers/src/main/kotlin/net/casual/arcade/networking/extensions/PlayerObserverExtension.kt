/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.networking.extensions

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.utils.register
import net.casual.arcade.extensions.PlayerExtension
import net.casual.arcade.extensions.event.PlayerExtensionEvent
import net.casual.arcade.extensions.utils.getExtension
import net.casual.arcade.networking.observer.PlayerObserver
import net.minecraft.server.level.ServerPlayer

internal class PlayerObserverExtension(player: ServerPlayer): PlayerExtension(player) {
    val observer = PlayerObserver(player.connection)

    companion object {
        val ServerPlayer.observerExtension: PlayerObserverExtension
            get() = this.getExtension()

        fun registerEvents() {
            GlobalEventHandler.Server.register<PlayerExtensionEvent> {
                it.addExtension(::PlayerObserverExtension)
            }
        }
    }
}