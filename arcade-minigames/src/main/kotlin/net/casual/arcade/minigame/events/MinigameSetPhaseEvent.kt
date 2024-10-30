package net.casual.arcade.minigame.events

import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.phase.Phase

public class MinigameSetPhaseEvent(
    override val minigame: Minigame,
    public val phase: Phase<Minigame>,
    public val previous: Phase<Minigame>
): MinigameEvent