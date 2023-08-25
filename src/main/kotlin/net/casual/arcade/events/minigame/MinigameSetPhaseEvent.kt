package net.casual.arcade.events.minigame

import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.MinigamePhase

class MinigameSetPhaseEvent(
    override val minigame: Minigame,
    val phase: MinigamePhase
) : MinigameEvent