/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.task.routine

import net.casual.arcade.minigame.Minigame
import net.casual.arcade.scheduler.task.routine.Routine
import net.casual.arcade.scheduler.task.routine.RoutineScope

/**
 * A [Routine] which runs against a [Minigame].
 *
 * @param M The minigame type.
 * @see Routine
 */
public interface MinigameRoutine<M: Minigame>: Routine<M>

/**
 * The minigame this routine is running for.
 *
 * This is an alias for [RoutineScope.owner].
 */
public val <M: Minigame> RoutineScope<M>.minigame: M
    get() = this.owner
