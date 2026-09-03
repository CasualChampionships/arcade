/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.tests.server.minigame.utils

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.minigame.task.routine.MinigameRoutine
import net.casual.arcade.minigame.task.routine.minigame
import net.casual.arcade.scheduler.task.routine.Routine
import net.casual.arcade.scheduler.task.routine.RoutineScope
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.arcade
import net.casual.arcade.utils.serialization.codec.CodecProvider
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.minecraft.resources.Identifier

class TestGraceRoutine(
    private val duration: MinecraftTimeDuration
): MinigameRoutine<TestMinigame> {
    override fun codec(): MapCodec<out Routine<TestMinigame>> {
        return codec
    }

    override suspend fun RoutineScope<TestMinigame>.run() {
        minigame.record(TestMinigameStage.GraceHeld)
        try {
            step(TestMinigameStage.GraceStarted.name) { minigame.record(TestMinigameStage.GraceStarted) }
            delay(duration) { remaining -> minigame.score = remaining.ticks }
            step(TestMinigameStage.GraceEnded.name) { minigame.record(TestMinigameStage.GraceEnded) }
        } finally {
            minigame.record(TestMinigameStage.GraceReleased)
        }
    }

    companion object: CodecProvider<TestGraceRoutine> {
        override val id: Identifier = arcade("test_grace")
        override val codec: MapCodec<TestGraceRoutine> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.INT.fieldOf("duration").forGetter { it.duration.ticks }
            ).apply(instance) { ticks -> TestGraceRoutine(ticks.Ticks) }
        }
    }
}

class TestActiveRoutine: MinigameRoutine<TestMinigame> {
    override fun codec(): MapCodec<out Routine<TestMinigame>> {
        return codec
    }

    override suspend fun RoutineScope<TestMinigame>.run() {
        step(TestMinigameStage.ActiveStarted.name) { minigame.record(TestMinigameStage.ActiveStarted) }
        while (true) {
            delay(20.Ticks)
        }
    }

    companion object: CodecProvider<TestActiveRoutine> {
        override val id: Identifier = arcade("test_active")
        override val codec: MapCodec<TestActiveRoutine> = MapCodec.unit(::TestActiveRoutine)
    }
}
