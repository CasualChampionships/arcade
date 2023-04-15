package net.casualuhc.arcade.events.player

import net.casualuhc.arcade.events.core.CancellableEvent
import net.minecraft.network.chat.PlayerChatMessage
import net.minecraft.server.level.ServerPlayer

data class PlayerChatEvent(
    val player: ServerPlayer,
    val message: PlayerChatMessage
): CancellableEvent.Default()