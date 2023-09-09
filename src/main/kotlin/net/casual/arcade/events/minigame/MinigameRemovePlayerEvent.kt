package net.casual.arcade.events.minigame

import net.casual.arcade.minigame.Minigame
import net.minecraft.server.level.ServerPlayer

data class MinigameRemovePlayerEvent(
    override val minigame: Minigame<*>,
    val player: ServerPlayer
): MinigameEvent
