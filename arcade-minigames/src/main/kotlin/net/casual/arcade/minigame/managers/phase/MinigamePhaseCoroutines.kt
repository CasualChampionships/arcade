/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.managers.phase

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.managers.MinigamePhaseManager
import net.casual.arcade.minigame.phase.MinigamePhase
import net.casual.arcade.minigame.serialization.SerializableMinigame
import net.casual.arcade.scheduler.task.routine.Routine

/**
 * This class is responsible for registering coroutines
 * tied to [MinigamePhase]s.
 *
 * This is the non-serializable counterpart to [MinigamePhaseRoutines].
 */
public class MinigamePhaseCoroutines internal constructor(
    private val phases: MinigamePhaseManager,
    private val minigame: Minigame
) {
    private val coroutines = Reference2ObjectOpenHashMap<MinigamePhase, suspend () -> Unit>()

    /**
     * Gets the coroutine to run for the specified [phase].
     *
     * @param phase The phase to get the coroutine for.
     * @return The coroutine, `null` if none is set.
     */
    public operator fun get(phase: MinigamePhase): (suspend () -> Unit)? {
        return this.coroutines[phase]
    }

    /**
     * Sets the coroutine for a specific [phase].
     *
     * @param phase The phase to set the coroutine for.
     * @param block The coroutine to run for the phase.
     * @throws IllegalArgumentException If [phase] is not a valid phase for the minigame,
     *   if the minigame is a [SerializableMinigame], or if the minigame already
     *   uses [Routine]s for its phases.
     */
    public operator fun set(phase: MinigamePhase, block: suspend () -> Unit) {
        require(this.phases.contains(phase)) {
            "Phase ${phase.id} is not a phase of minigame ${this.minigame.id}"
        }
        require(this.minigame !is SerializableMinigame) {
            "Minigame ${this.minigame.id} is serializable, phase ${phase.id} must use a routine"
        }
        require(this.phases.routines.isEmpty()) {
            "Minigame ${this.minigame.id} already uses routines for its phases, you cannot mix the two"
        }
        this.coroutines[phase] = block
    }

    /**
     * Whether a coroutine exists for the given [phase].
     *
     * @param phase The phase to check.
     * @return Whether a coroutine has been set.
     */
    public operator fun contains(phase: MinigamePhase): Boolean {
        return this.coroutines.containsKey(phase)
    }

    internal fun isEmpty(): Boolean {
        return this.coroutines.isEmpty()
    }

    /**
     * Removes the coroutine for the given [phase].
     *
     * @param phase The phase to remove.
     * @return The coroutine that was associated with that phase,
     *   `null` if there was none.
     */
    public fun remove(phase: MinigamePhase): (suspend () -> Unit)? {
        return this.coroutines.remove(phase)
    }
}
