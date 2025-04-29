/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.player

import net.casual.arcade.events.common.CancellableEvent
import net.casual.arcade.utils.chat.PlayerFormattedChat
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.PlayerChatMessage
import net.minecraft.server.level.ServerPlayer
import java.util.function.Predicate

public data class PlayerChatEvent(
    override val player: ServerPlayer,
    val message: PlayerChatMessage
): CancellableEvent.Default(), PlayerEvent {
    private val filters = ArrayList<Predicate<ServerPlayer>>()
    private var formatted: PlayerFormattedChat = PlayerFormattedChat(message = this.message.decoratedContent())
    private var mutated: Boolean = false

    val rawMessage: String
        get() = this.message.signedContent()

    public fun formatted(): PlayerFormattedChat {
        return this.formatted
    }

    public fun format(mutator: (PlayerFormattedChat) -> PlayerFormattedChat) {
        this.formatted = mutator.invoke(this.formatted)
        this.mutated = true
    }

    public fun hasMutated(): Boolean {
        return this.mutated
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