/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.player

import com.mojang.authlib.GameProfile
import net.casual.arcade.events.common.Event
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import java.net.SocketAddress

public data class PlayerRequestLoginEvent(
    val server: MinecraftServer,
    val profile: GameProfile,
    val address: SocketAddress
): Event {
    val isAccepted: Boolean
        get() = this.reason == null

    var reason: Component? = null
        private set

    public fun accept() {
        this.reason = null
    }

    public fun deny(reason: Component) {
        this.reason = reason
    }
}