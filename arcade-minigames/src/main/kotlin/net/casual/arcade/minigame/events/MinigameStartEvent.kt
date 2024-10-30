package net.casual.arcade.minigame.events

import net.casual.arcade.minigame.Minigame

public data class MinigameStartEvent(
    override val minigame: Minigame
): MinigameEvent