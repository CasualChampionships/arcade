package net.casualuhc.arcade.events.minigame

import net.casualuhc.arcade.events.core.CancellableEvent
import net.casualuhc.arcade.minigame.Minigame
import net.minecraft.server.level.ServerPlayer

data class MinigameAddPlayerEvent(
    override val minigame: Minigame,
    val player: ServerPlayer
): CancellableEvent.Default(), MinigameEvent