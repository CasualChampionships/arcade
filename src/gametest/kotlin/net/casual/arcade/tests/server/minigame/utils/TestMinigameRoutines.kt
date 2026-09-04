/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.tests.server.minigame.utils

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.minigame.routine.MinigameRoutine
import net.casual.arcade.minigame.routine.minigame
import net.casual.arcade.minigame.routine.requestPhase
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

class TestRoundRoutine(
    private val rounds: Int,
    private val phase: TestMinigamePhase
): MinigameRoutine<TestMinigame> {
    override fun codec(): MapCodec<out Routine<TestMinigame>> {
        return codec
    }

    override suspend fun RoutineScope<TestMinigame>.run() {
        step("round") {
            minigame.score += 1
            minigame.record(TestMinigameStage.RoundPlayed)
        }
        if (minigame.score < rounds) {
            requestPhase(phase)
        }
    }

    companion object: CodecProvider<TestRoundRoutine> {
        override val id: Identifier = arcade("test_round")
        override val codec: MapCodec<TestRoundRoutine> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.INT.fieldOf("rounds").forGetter(TestRoundRoutine::rounds),
                TestMinigamePhase.CODEC.fieldOf("phase").forGetter(TestRoundRoutine::phase)
            ).apply(instance, ::TestRoundRoutine)
        }
    }
}

class TestSettingRoutine(
    private val phase: TestMinigamePhase
): MinigameRoutine<TestMinigame> {
    override fun codec(): MapCodec<out Routine<TestMinigame>> {
        return codec
    }

    override suspend fun RoutineScope<TestMinigame>.run() {
        try {
            step(TestMinigameStage.PhaseSet.name) {
                minigame.phases.set(phase)
                minigame.observedPhase = minigame.phaseOrNull
                minigame.record(TestMinigameStage.PhaseSet)
            }
            step(TestMinigameStage.AfterPhaseSet.name) { minigame.record(TestMinigameStage.AfterPhaseSet) }
        } finally {
            minigame.record(TestMinigameStage.PhaseSetReleased)
        }
    }

    companion object: CodecProvider<TestSettingRoutine> {
        override val id: Identifier = arcade("test_setting")
        override val codec: MapCodec<TestSettingRoutine> = TestMinigamePhase.CODEC.fieldOf("phase")
            .xmap(::TestSettingRoutine, TestSettingRoutine::phase)
    }
}
