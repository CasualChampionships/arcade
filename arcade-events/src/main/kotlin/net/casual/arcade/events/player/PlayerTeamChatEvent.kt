package net.casual.arcade.events.player

import net.casual.arcade.events.core.CancellableEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.PlayerChatMessage
import net.minecraft.server.level.ServerPlayer

public data class PlayerTeamChatEvent(
    override val player: ServerPlayer,
    val message: PlayerChatMessage,
    val teammates: List<ServerPlayer>
): CancellableEvent.Default(), PlayerEvent {
    private var prefix: Component? = null
    private var replacement: Component? = null

    private val receiving = HashSet<ServerPlayer>(this.teammates)

    val rawMessage: String
        get() = this.message.signedContent()

    public fun replaceMessage(component: Component, prefix: Component? = null) {
        this.replacement = component
        this.prefix = prefix
    }

    public fun getReplacementMessage(): Component? {
        return this.replacement
    }

    public fun getMessagePrefix(): Component? {
        return this.prefix
    }

    public fun addReceiver(player: ServerPlayer) {
        this.receiving.add(player)
    }

    public fun getReceiving(): Set<ServerPlayer> {
        return this.receiving
    }
}