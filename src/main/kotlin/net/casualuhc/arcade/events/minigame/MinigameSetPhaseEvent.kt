package net.casualuhc.arcade.events.minigame

import net.casualuhc.arcade.minigame.Minigame
import net.casualuhc.arcade.minigame.MinigamePhase

class MinigameSetPhaseEvent(
    override val minigame: Minigame,
    val phase: MinigamePhase
) : MinigameEvent