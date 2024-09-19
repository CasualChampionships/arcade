package net.casual.arcade.minigame.events

import net.casual.arcade.minigame.Minigame

public data class MinigameCompleteEvent(
    override val minigame: Minigame<*>
): MinigameEvent