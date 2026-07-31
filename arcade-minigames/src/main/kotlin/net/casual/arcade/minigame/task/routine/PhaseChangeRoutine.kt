/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.task.routine

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.scheduler.task.routine.Routine
import net.casual.arcade.scheduler.task.routine.RoutineScope
import net.casual.arcade.utils.arcade
import net.casual.arcade.utils.serialization.codec.CodecProvider
import net.minecraft.resources.Identifier

/**
 * A [MinigameRoutine] which sets the minigame's phase.
 *
 * @param phase The id of the phase to change to.
 */
public class PhaseChangeRoutine(
    private val phase: String
): MinigameRoutine<Minigame> {
    override fun codec(): MapCodec<out Routine<Minigame>> {
        return codec
    }

    override suspend fun RoutineScope<Minigame>.run() {
        val found = minigame.getPhase(phase) ?: return
        minigame.setPhase(found)
    }

    public companion object: CodecProvider<PhaseChangeRoutine> {
        override val id: Identifier = arcade("phase_change")

        override val codec: MapCodec<out PhaseChangeRoutine> = Codec.STRING.fieldOf("phase")
            .xmap(::PhaseChangeRoutine, PhaseChangeRoutine::phase)
    }
}
