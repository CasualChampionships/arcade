package net.casual.arcade.events.minigame

import net.casual.arcade.minigame.Minigame

public class MinigamePauseEvent(
    override val minigame: Minigame<*>
) : MinigameEvent