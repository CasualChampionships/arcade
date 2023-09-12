package net.casual.arcade.events.player

import net.casual.arcade.events.core.CancellableEvent
import net.minecraft.network.chat.PlayerChatMessage
import net.minecraft.server.level.ServerPlayer

public data class PlayerChatEvent(
    override val player: ServerPlayer,
    val message: PlayerChatMessage
): CancellableEvent.Default(), PlayerEvent