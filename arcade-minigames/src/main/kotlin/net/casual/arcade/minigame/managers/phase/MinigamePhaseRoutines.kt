/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.managers.phase

import com.google.common.reflect.TypeToken
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.managers.MinigamePhaseManager
import net.casual.arcade.minigame.phase.MinigamePhase
import net.casual.arcade.scheduler.task.routine.Routine
import net.casual.arcade.scheduler.utils.throwIfNotRegistered
import java.util.function.Consumer

/**
 * This class is responsible for registering [Routine]s
 * tied to [MinigamePhase]s.
 */
public class MinigamePhaseRoutines internal constructor(
    private val phases: MinigamePhaseManager,
    private val minigame: Minigame
) {
    private val routines = Reference2ObjectOpenHashMap<MinigamePhase, Routine<Minigame>>()

    /**
     * Gets the [Routine] to run for the specified [phase].
     *
     * @param phase The phase to get the [Routine] for.
     * @return The [Routine], `null` if none is set.
     */
    public operator fun get(phase: MinigamePhase): Routine<Minigame>? {
        return this.routines[phase]
    }

    /**
     * Sets a [Routine] for a specific [phase].
     *
     * The routine type must match `Routine<in M>`
     * where `M` is your concrete [Minigame] implementation.
     *
     * @param phase The phase to set the routine for.
     * @param routine The routine.
     * @throws IllegalArgumentException If [phase] is not a valid phase for the minigame,
     *   if the [routine] isn't registered, or if the [routine] isn't valid for this minigame.
     */
    public operator fun set(phase: MinigamePhase, routine: Routine<out Minigame>) {
        require(this.phases.contains(phase)) {
            "Phase ${phase.id} is not a phase of minigame ${this.minigame.id}"
        }
        this.routines[phase] = this.validate(routine)
    }

    /**
     * Whether a [Routine] exists for the given [phase].
     *
     * @param phase The phase to check.
     * @return Whether a routine has been set.
     */
    public operator fun contains(phase: MinigamePhase): Boolean {
        return this.routines.containsKey(phase)
    }

    /**
     * Removes the routine for the given [phase].
     *
     * @param phase The phase to remove.
     * @return The routine that was associated with that phase,
     *   `null` if there was none.
     */
    public fun remove(phase: MinigamePhase): Routine<Minigame>? {
        return this.routines.remove(phase)
    }

    private fun validate(routine: Routine<out Minigame>): Routine<Minigame> {
        routine.throwIfNotRegistered()

        val type = TypeToken.of(routine.javaClass).resolveType(Routine::class.java.typeParameters[0]).rawType
        require(type.isInstance(this.minigame)) {
            "Routine ${this.javaClass.name} is not valid for minigame ${this.minigame.id}"
        }
        @Suppress("UNCHECKED_CAST")
        return routine as Routine<Minigame>
    }
}
