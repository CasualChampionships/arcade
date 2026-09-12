/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.managers.phase

import com.mojang.serialization.MapCodec
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.routine.MinigameRoutine
import net.casual.arcade.minigame.routine.minigame
import net.casual.arcade.scheduler.task.routine.Routine
import net.casual.arcade.scheduler.task.routine.RoutineScope
import net.casual.arcade.scheduler.utils.call
import net.casual.arcade.utils.arcade
import net.casual.arcade.utils.serialization.codec.CodecProvider
import net.minecraft.resources.Identifier

internal class AdvancingPhaseRoutine(
    private val routine: Routine<Minigame>
): MinigameRoutine<Minigame> {
    override val version: Int
        get() = this.routine.version

    override fun codec(): MapCodec<out Routine<Minigame>> {
        return codec
    }

    override suspend fun RoutineScope<Minigame>.run() {
        call(routine)
        step("advance_phase") { minigame.phases.tryRequestAdvance() }
    }

    companion object: CodecProvider<AdvancingPhaseRoutine> {
        override val id: Identifier = arcade("phase")

        @Suppress("UNCHECKED_CAST")
        override val codec: MapCodec<out AdvancingPhaseRoutine> = Routine.CODEC.fieldOf("routine").xmap(
            { AdvancingPhaseRoutine(it as Routine<Minigame>) }, AdvancingPhaseRoutine::routine
        )
    }
}
