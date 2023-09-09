package net.casual.arcade.events.minigame

import net.casual.arcade.minigame.Minigame

class MinigameUnpauseEvent(
    override val minigame: Minigame<*>
) : MinigameEvent