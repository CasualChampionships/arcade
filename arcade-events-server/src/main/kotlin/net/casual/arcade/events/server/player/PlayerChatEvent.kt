package net.casual.arcade.events.server.player

import net.casual.arcade.events.common.CancellableEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.PlayerChatMessage
import net.minecraft.server.level.ServerPlayer
import java.util.function.Predicate

public data class PlayerChatEvent(
    override val player: ServerPlayer,
    val message: PlayerChatMessage
): CancellableEvent.Default(), PlayerEvent {
    private val filters = ArrayList<Predicate<ServerPlayer>>()
    private var prefix: Component? = null
    private var replacement: Component? = null

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

    public fun addFilter(filter: Predicate<ServerPlayer>) {
        this.filters.add(filter)
    }

    public fun getFilter(): Predicate<ServerPlayer>? {
        if (this.filters.isEmpty()) {
            return null
        }
        return this.filters.reduce { acc, predicate -> acc.and(predicate) }
    }
}