package net.casual.arcade.minigame.events

import net.casual.arcade.minigame.Minigame

public class MinigameUnpauseEvent(
    override val minigame: Minigame<*>
) : MinigameEvent