/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.events

import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.phase.Phase

public class MinigameSetPhaseEvent(
    override val minigame: Minigame,
    public val phase: Phase<Minigame>,
    public val previous: Phase<Minigame>
): MinigameEvent