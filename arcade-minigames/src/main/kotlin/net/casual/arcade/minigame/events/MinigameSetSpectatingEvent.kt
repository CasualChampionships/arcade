package net.casual.arcade.minigame.events

import net.casual.arcade.minigame.Minigame
import net.minecraft.server.level.ServerPlayer

public data class MinigameSetSpectatingEvent(
    override val minigame: Minigame<*>,
    val player: ServerPlayer
): MinigameEvent