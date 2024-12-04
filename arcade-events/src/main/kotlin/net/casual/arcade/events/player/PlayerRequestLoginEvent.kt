package net.casual.arcade.events.player

import com.mojang.authlib.GameProfile
import net.casual.arcade.events.core.Event
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