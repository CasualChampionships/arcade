package net.casual.arcade.events.minigame

import net.casual.arcade.minigame.Minigame

public data class MinigameStartEvent(
    override val minigame: Minigame<*>
): MinigameEvent