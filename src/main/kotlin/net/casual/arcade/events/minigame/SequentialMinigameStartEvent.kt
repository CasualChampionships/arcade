package net.casual.arcade.events.minigame

import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.events.SequentialMinigames

public data class SequentialMinigameStartEvent(
    override val minigame: Minigame<*>,
    val minigames: SequentialMinigames
): MinigameEvent