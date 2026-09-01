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

public class MinigamePhaseRoutines internal constructor(
    private val phases: MinigamePhaseManager,
    private val minigame: Minigame
) {
    private val routines = Reference2ObjectOpenHashMap<MinigamePhase, Routine<Minigame>>()

    public operator fun get(phase: MinigamePhase): Routine<Minigame>? {
        return this.routines[phase]
    }

    public operator fun set(phase: MinigamePhase, routine: Routine<out Minigame>) {
        require(this.phases.contains(phase)) {
            "Phase ${phase.id} is not a phase of minigame ${this.minigame.id}"
        }
        this.routines[phase] = this.validate(routine)
    }

    public operator fun contains(phase: MinigamePhase): Boolean {
        return this.routines.containsKey(phase)
    }

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
