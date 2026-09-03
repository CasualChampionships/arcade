/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.tests.server.minigame

import net.casual.arcade.gametest.TestContext
import net.casual.arcade.tests.server.ArcadeTestSuite
import net.casual.arcade.tests.server.minigame.utils.TestMinigamePhase.Active
import net.casual.arcade.tests.server.minigame.utils.TestMinigamePhase.Grace
import net.casual.arcade.tests.server.minigame.utils.TestMinigamePhase.Over
import net.casual.arcade.tests.server.minigame.utils.TestMinigameStage.AfterPhaseSet
import net.casual.arcade.tests.server.minigame.utils.TestMinigameStage.PhaseSet
import net.casual.arcade.tests.server.minigame.utils.TestMinigameStage.PhaseSetReleased
import net.casual.arcade.tests.server.minigame.utils.TestMinigameStage.RoundPlayed
import net.casual.arcade.tests.server.minigame.utils.TestRoundRoutine
import net.casual.arcade.tests.server.minigame.utils.TestSettingRoutine
import net.casual.arcade.tests.server.minigame.utils.minigame
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.TimeUtils.Seconds
import net.fabricmc.fabric.api.gametest.v1.GameTest

@Suppress("FunctionName", "Unused")
object MinigamePhaseTransitionTests: ArcadeTestSuite() {
    override val namespace: String = ArcadeUtils.MOD_ID

    @GameTest(maxTicks = 200)
    fun `phase routine advances to the next phase by default`(context: TestContext) = context.test {
        val minigame = minigame().configure { game ->
            game.phases.routines.remove(Grace)
            game.phases.routines[Active] = TestRoundRoutine(rounds = 1, phase = Active)
        }.phase(Active).start()

        assertEventually(5.Seconds, "Phase routine did not advance to the next phase") {
            minigame.phaseOrNull == Over
        }
        assertEquals(1, minigame.score, "Phase routine ran more than once")
    }

    @GameTest(maxTicks = 200)
    fun `phase routine can request an earlier phase`(context: TestContext) = context.test {
        val minigame = minigame().configure { game ->
            game.phases.routines.remove(Grace)
            game.phases.routines[Active] = TestRoundRoutine(rounds = 2, phase = Grace)
        }.phase(Active).start()

        assertEventually(5.Seconds, "Phase routine did not move back to an earlier phase") {
            minigame.phaseOrNull == Grace
        }
        assertNever(1.Seconds, "Phase routine advanced despite requesting an earlier phase") {
            minigame.phaseOrNull == Over
        }
    }

    @GameTest(maxTicks = 200)
    fun `phase routine can repeat its own phase`(context: TestContext) = context.test {
        val minigame = minigame().configure { game ->
            game.phases.routines.remove(Grace)
            game.phases.routines[Active] = TestRoundRoutine(rounds = 3, phase = Active)
        }.phase(Active).start()

        assertEventually(5.Seconds, "Phase routine did not repeat its own phase") {
            minigame.score == 3
        }
        assertEquals(
            listOf(RoundPlayed, RoundPlayed, RoundPlayed),
            minigame.recordedStages,
            "Repeating a phase did not re-run its routine once per round"
        )
    }

    @GameTest(maxTicks = 200)
    fun `phase routine advances once it stops requesting`(context: TestContext) = context.test {
        val minigame = minigame().configure { game ->
            game.phases.routines.remove(Grace)
            game.phases.routines[Active] = TestRoundRoutine(rounds = 2, phase = Active)
        }.phase(Active).start()

        assertEventually(5.Seconds, "Phase routine did not advance after its last round") {
            minigame.phaseOrNull == Over
        }
        assertEquals(2, minigame.score, "Phase routine did not play every round before advancing")
    }

    @GameTest(maxTicks = 200)
    fun `setting the phase inside a routine is deferred`(context: TestContext) = context.test {
        val minigame = minigame().configure { game ->
            game.phases.routines.remove(Grace)
            game.phases.routines[Active] = TestSettingRoutine(Over)
        }.phase(Active).start()

        assertEventually(5.Seconds, "Phase set inside a routine was never applied") {
            minigame.phaseOrNull == Over
        }
        assertEquals(Active, minigame.observedPhase, "Phase set inside a routine was applied immediately")
    }

    @GameTest(maxTicks = 200)
    fun `setting the phase inside a routine lets it finish`(context: TestContext) = context.test {
        val minigame = minigame().configure { game ->
            game.phases.routines.remove(Grace)
            game.phases.routines[Active] = TestSettingRoutine(Over)
        }.phase(Active).start()

        assertEventually(5.Seconds, "Phase set inside a routine was never applied") {
            minigame.phaseOrNull == Over
        }
        assertEquals(
            listOf(PhaseSet, AfterPhaseSet, PhaseSetReleased),
            minigame.recordedStages,
            "Setting the phase inside a routine did not let the routine run to completion"
        )
    }

    @GameTest(maxTicks = 200)
    fun `setting the phase inside a routine overrides the automatic advance`(context: TestContext) = context.test {
        val minigame = minigame().configure { game ->
            game.phases.routines.remove(Grace)
            game.phases.routines[Active] = TestSettingRoutine(Grace)
        }.phase(Active).start()

        assertEventually(5.Seconds, "Phase set inside a routine was never applied") {
            minigame.phaseOrNull == Grace
        }
    }
}
