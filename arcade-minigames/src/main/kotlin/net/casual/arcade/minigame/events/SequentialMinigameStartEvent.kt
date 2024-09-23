package net.casual.arcade.minigame.events

import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.template.minigame.SequentialMinigames

public data class SequentialMinigameStartEvent(
    override val minigame: Minigame<*>,
    val minigames: SequentialMinigames
): MinigameEvent