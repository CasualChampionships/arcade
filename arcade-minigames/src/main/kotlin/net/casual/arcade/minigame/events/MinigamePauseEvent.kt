package net.casual.arcade.minigame.events

import net.casual.arcade.minigame.Minigame

public class MinigamePauseEvent(
    override val minigame: Minigame
) : MinigameEvent