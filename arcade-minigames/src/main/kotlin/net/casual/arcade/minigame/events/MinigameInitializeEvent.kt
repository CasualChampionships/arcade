package net.casual.arcade.minigame.events

import net.casual.arcade.minigame.Minigame

public data class MinigameInitializeEvent(
    override val minigame: Minigame
): MinigameEvent