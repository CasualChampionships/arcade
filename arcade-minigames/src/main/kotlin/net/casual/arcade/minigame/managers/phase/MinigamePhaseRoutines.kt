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
import net.casual.arcade.utils.ArcadeUtils
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import kotlin.jvm.optionals.getOrNull

/**
 * This class is responsible for registering [Routine]s
 * tied to [MinigamePhase]s.
 *
 * This is the serializable counterpart to [MinigamePhaseCoroutines].
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
     *   if the [routine] isn't registered, if the [routine] isn't valid for this minigame,
     *   or if the minigame already uses coroutines for its phases.
     */
    public operator fun set(phase: MinigamePhase, routine: Routine<out Minigame>) {
        require(this.phases.contains(phase)) {
            "Phase ${phase.id} is not a phase of minigame ${this.minigame.id}"
        }
        require(this.phases.coroutines.isEmpty()) {
            "Minigame ${this.minigame.id} already uses coroutines for its phases, you cannot mix the two"
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

    internal fun isEmpty(): Boolean {
        return this.routines.isEmpty()
    }

    internal fun serialize(output: ValueOutput.ValueOutputList) {
        for (phase in this.phases) {
            val routine = this.routines[phase] ?: continue
            val data = output.addChild()
            data.store("phase", this.phases.codec, phase)
            data.store("routine", Routine.CODEC, routine)
        }
    }

    internal fun deserialize(input: ValueInput.ValueInputList) {
        this.routines.clear()
        for (data in input) {
            val phase = data.read("phase", this.phases.codec).getOrNull() ?: continue
            val routine = data.read("routine", Routine.CODEC).getOrNull() ?: continue
            if (!this.isValidFor(routine)) {
                ArcadeUtils.logger.error("Routine ${routine.javaClass.name} for phase ${phase.id} became invalid after reload!?")
                continue
            }
            @Suppress("UNCHECKED_CAST")
            this.routines[phase] = routine as Routine<Minigame>
        }
    }

    private fun validate(routine: Routine<out Minigame>): Routine<Minigame> {
        routine.throwIfNotRegistered()

        require(this.isValidFor(routine)) {
            "Routine ${routine.javaClass.name} is not valid for minigame ${this.minigame.id}"
        }
        @Suppress("UNCHECKED_CAST")
        return routine as Routine<Minigame>
    }

    private fun isValidFor(routine: Routine<*>): Boolean {
        val type = TypeToken.of(routine.javaClass).resolveType(Routine::class.java.typeParameters[0]).rawType
        return type.isInstance(this.minigame)
    }
}
