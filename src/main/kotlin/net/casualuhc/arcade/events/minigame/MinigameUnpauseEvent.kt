package net.casualuhc.arcade.events.minigame

import net.casualuhc.arcade.minigame.Minigame

class MinigameUnpauseEvent(
    override val minigame: Minigame
) : MinigameEvent