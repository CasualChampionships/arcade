package net.casualuhc.arcade.events.minigame

import net.casualuhc.arcade.minigame.Minigame

class MinigameCloseEvent(
    override val minigame: Minigame
) : MinigameEvent