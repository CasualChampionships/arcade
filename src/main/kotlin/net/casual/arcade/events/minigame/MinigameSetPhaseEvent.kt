package net.casual.arcade.events.minigame

import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.phase.Phase

public class MinigameSetPhaseEvent<M: Minigame<M>>(
    override val minigame: M,
    public val phase: Phase<M>,
    public val previous: Phase<M>
) : MinigameEvent