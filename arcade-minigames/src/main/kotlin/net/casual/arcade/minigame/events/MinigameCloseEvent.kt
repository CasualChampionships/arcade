package net.casual.arcade.minigame.events

import net.casual.arcade.minigame.Minigame

public class MinigameCloseEvent(
    override val minigame: Minigame<*>
): MinigameEvent