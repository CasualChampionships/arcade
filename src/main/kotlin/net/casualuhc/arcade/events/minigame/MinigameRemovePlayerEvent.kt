package net.casualuhc.arcade.events.minigame

import net.casualuhc.arcade.minigame.Minigame
import net.minecraft.server.level.ServerPlayer

data class MinigameRemovePlayerEvent(
    override val minigame: Minigame,
    val player: ServerPlayer
): MinigameEvent
