/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.tests.server.minigame

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.utils.register
import net.casual.arcade.gametest.TestContext
import net.casual.arcade.minigame.MinigameState
import net.casual.arcade.minigame.Minigames
import net.casual.arcade.minigame.events.MinigameCloseEvent
import net.casual.arcade.minigame.events.MinigameCompleteEvent
import net.casual.arcade.minigame.phase.MinigamePhaseLifetime
import net.casual.arcade.tests.server.ArcadeTestSuite
import net.casual.arcade.tests.server.minigame.utils.TestMinigamePhase.Active
import net.casual.arcade.tests.server.minigame.utils.TestMinigamePhase.Grace
import net.casual.arcade.tests.server.minigame.utils.TestMinigameStage.GraceReleased
import net.casual.arcade.tests.server.minigame.utils.TestScoreComponent
import net.casual.arcade.tests.server.minigame.utils.minigame
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.coroutine.delay
import net.fabricmc.fabric.api.gametest.v1.GameTest

@Suppress("FunctionName", "Unused")
object MinigameTeardownTests: ArcadeTestSuite() {
    override val namespace: String = ArcadeUtils.MOD_ID

    @GameTest
    fun `scope registered close listener fires`(context: TestContext) = context.test {
        val minigame = minigame().withoutPhaseRoutines().phase(Grace).start()
        val scope = minigame.scopes.create(MinigamePhaseLifetime.Forever)

        var closed = false
        scope.register<MinigameCloseEvent> { closed = true }

        minigame.close()

        assertTrue(closed, "Scope listeners were unregistered before MinigameCloseEvent was broadcast")
    }

    @GameTest
    fun `close listener still sees the minigame playing`(context: TestContext) = context.test {
        val minigame = minigame().withoutPhaseRoutines().phase(Grace).start()

        var state: MinigameState? = null
        var started = false
        minigame.scopes.root.register<MinigameCloseEvent> {
            state = minigame.state
            started = minigame.started
        }

        minigame.close()

        assertEquals(MinigameState.Playing(Grace), state, "Minigame reported closed while it was tearing down")
        assertTrue(started, "Minigame stopped reporting as started during teardown")
    }

    @GameTest
    fun `close listener still sees the players`(context: TestContext) = context.test {
        val minigame = minigame().withoutPhaseRoutines().phase(Grace).start()
        minigame.players.add(player().spawn())

        var players = -1
        minigame.scopes.root.register<MinigameCloseEvent> {
            players = minigame.players.all.size
        }

        minigame.close()

        assertEquals(1, players, "Players were removed before MinigameCloseEvent was broadcast")
    }

    @GameTest(maxTicks = 60)
    fun `cancelled routine unwinds before the players are removed`(context: TestContext) = context.test {
        val minigame = minigame().phase(Grace).start()
        minigame.players.add(player().spawn())
        delay(5.Ticks)

        minigame.close()

        assertTrue(minigame.recordedStages.contains(GraceReleased), "Closing did not cancel the running phase routine")
        assertEquals(
            1,
            minigame.playersAtStage[GraceReleased],
            "Routine cleanup ran against a minigame whose players had already been removed"
        )
    }

    @GameTest
    fun `closing closes every component`(context: TestContext) = context.test {
        val component = TestScoreComponent()
        val minigame = minigame().withoutPhaseRoutines().component(component).start()

        minigame.close()

        assertTrue(component.closed, "Component was not closed with its minigame")
    }

    @GameTest
    fun `close is idempotent`(context: TestContext) = context.test {
        val minigame = minigame().withoutPhaseRoutines().phase(Grace).start()

        var closes = 0
        val handle = GlobalEventHandler.Server.register<MinigameCloseEvent> {
            if (it.minigame === minigame) {
                closes += 1
            }
        }

        try {
            minigame.close()
            minigame.close()
        } finally {
            handle.remove()
        }

        assertEquals(1, closes, "Closing twice broadcast MinigameCloseEvent twice")
    }

    @GameTest
    fun `complete is terminal and broadcasts once`(context: TestContext) = context.test {
        val minigame = minigame().withoutPhaseRoutines().phase(Grace).start()

        var completions = 0
        val handle = GlobalEventHandler.Server.register<MinigameCompleteEvent> {
            if (it.minigame === minigame) {
                completions += 1
            }
        }

        try {
            minigame.complete()
            minigame.complete()
        } finally {
            handle.remove()
        }

        assertEquals(1, completions, "Completing twice broadcast MinigameCompleteEvent twice")
        assertEquals(MinigameState.Closed(completed = true), minigame.state)
    }

    @GameTest
    fun `closing without completing is not completed`(context: TestContext) = context.test {
        val minigame = minigame().withoutPhaseRoutines().phase(Grace).start()

        minigame.close()

        assertEquals(MinigameState.Closed(completed = false), minigame.state)
        assertFalse(minigame.completed, "A minigame which was closed reported itself as completed")
    }

    @GameTest
    fun `closed minigame is unregistered`(context: TestContext) = context.test {
        val minigame = minigame().withoutPhaseRoutines().phase(Grace).start()
        assertNotNull(Minigames.get(minigame.uuid), "Minigame was not registered while it was playing")

        minigame.close()

        assertNull(Minigames.get(minigame.uuid), "Closed minigame is still registered")
    }

    @GameTest
    fun `closed minigame cannot change phase`(context: TestContext) = context.test {
        val minigame = minigame().withoutPhaseRoutines().phase(Grace).start()
        minigame.close()

        assertThrows<IllegalStateException> {
            minigame.phases.set(Active)
        }
    }
}
