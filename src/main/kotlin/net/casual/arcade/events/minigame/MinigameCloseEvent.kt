package net.casual.arcade.events.minigame

import net.casual.arcade.minigame.Minigame

public class MinigameCloseEvent(
    override val minigame: Minigame<*>
): MinigameEvent