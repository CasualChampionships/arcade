package net.casual.arcade.events.minigame

import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.MinigamePhase

public class MinigameSetPhaseEvent<M: Minigame<M>>(
    override val minigame: M,
    public val phase: MinigamePhase<M>
) : MinigameEvent