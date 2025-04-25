/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.player

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import net.casual.arcade.events.common.CancellableEvent
import net.casual.arcade.utils.chat.PlayerFormattedChat
import net.minecraft.network.chat.PlayerChatMessage
import net.minecraft.server.level.ServerPlayer

public data class PlayerTeamChatEvent(
    override val player: ServerPlayer,
    val message: PlayerChatMessage,
    val teammates: List<ServerPlayer>
): CancellableEvent.Default(), PlayerEvent {
    private var formatted: PlayerFormattedChat = PlayerFormattedChat(message = this.message.decoratedContent())
    private var mutated: Boolean = false

    private val receiving = ReferenceOpenHashSet(this.teammates)

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

    public fun addReceiver(player: ServerPlayer) {
        this.receiving.add(player)
    }

    public fun getReceiving(): Set<ServerPlayer> {
        return this.receiving
    }
}