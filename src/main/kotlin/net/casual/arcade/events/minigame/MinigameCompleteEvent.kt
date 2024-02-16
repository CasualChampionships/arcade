package net.casual.arcade.events.minigame

import net.casual.arcade.minigame.Minigame

public data class MinigameCompleteEvent(
    override val minigame: Minigame<*>
): MinigameEvent