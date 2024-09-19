package net.casual.arcade.events.player

import com.llamalad7.mixinextras.injector.wrapoperation.Operation
import net.casual.arcade.events.core.CancellableEvent
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.players.PlayerList
import org.jetbrains.annotations.ApiStatus.Internal

public data class PlayerSystemMessageEvent(
    override val player: ServerPlayer,
    var message: Component,
    val isActionBar: Boolean
): CancellableEvent.Default(), PlayerEvent {
    val causer: ServerPlayer? = PlayerSystemMessageEvent.causer

    public companion object {
        private var causer: ServerPlayer? = null

        @Internal
        @JvmStatic
        public fun broadcast(
            causer: ServerPlayer,
            players: PlayerList,
            message: Component,
            bypassHiddenChat: Boolean,
            operation: Operation<Void>
        ) {
            try {
                this.causer = causer
                operation.call(players, message, bypassHiddenChat)
            } finally {
                this.causer = null
            }
        }
    }
}