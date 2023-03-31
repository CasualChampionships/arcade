package net.casualuhc.arcade.events.player

import net.casualuhc.arcade.events.core.CancellableEvent
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

class PlayerJoinEvent(
    val player: ServerPlayer
): CancellableEvent() {
    private var reason: Component? = null

    fun cancel(reason: Component) {
        this.reason = reason
        this.cancel()
    }

    fun getReason(): Component {
        return this.reason!!
    }
}