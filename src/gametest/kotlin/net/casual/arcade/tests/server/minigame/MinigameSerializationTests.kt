/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.tests.server.minigame

import net.casual.arcade.dimensions.level.CustomLevel
import net.casual.arcade.dimensions.utils.deleteCustomLevel
import net.casual.arcade.gametest.TestContext
import net.casual.arcade.gametest.minigame.reload
import net.casual.arcade.minigame.MinigameState
import net.casual.arcade.minigame.Minigames
import net.casual.arcade.minigame.exception.MinigameCreationException
import net.casual.arcade.minigame.managers.MinigameLevelManager.LevelOwnership
import net.casual.arcade.minigame.serialization.save
import net.casual.arcade.tests.server.ArcadeTestSuite
import net.casual.arcade.tests.server.minigame.utils.*
import net.casual.arcade.tests.server.minigame.utils.TestMinigameStage.*
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.TimeUtils.Seconds
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.coroutine.delay
import net.fabricmc.fabric.api.gametest.v1.GameTest

@Suppress("FunctionName", "Unused")
object MinigameSerializationTests: ArcadeTestSuite() {
    override val namespace: String = ArcadeUtils.MOD_ID

    @GameTest
    fun `minigame restores its phase`(context: TestContext) = context.test {
        val minigame = minigame().phase(TestMinigamePhase.Active).start()

        val restored = reload(minigame)

        assertEquals(MinigameState.Playing(TestMinigamePhase.Active), restored.state)
        assertTrue(restored.state >= TestMinigamePhase.Active, "Restored state did not compare at its phase")
    }

    @GameTest
    fun `unstarted minigame is restored as ready`(context: TestContext) = context.test {
        val minigame = minigame().create()

        val restored = reload(minigame)

        assertEquals(MinigameState.Ready, restored.state)
        assertNull(restored.phaseOrNull, "A minigame which never started came back with a phase")
    }

    @GameTest
    fun `custom state survives a round trip`(context: TestContext) = context.test {
        val minigame = minigame().score(41).start()

        val restored = reload(minigame)

        assertEquals(41, restored.score, "The minigame's own serialize/deserialize did not round trip")
    }

    @GameTest(maxTicks = 100)
    fun `suspended phase routine resumes where it was saved`(context: TestContext) = context.test {
        val minigame = minigame().start()

        delay(5.Ticks)
        minigame.recordedStages shouldEqual listOf(GraceHeld, GraceStarted)

        val restored = reload(minigame)

        assertEquals(
            listOf(GraceHeld),
            restored.recordedStages,
            "Restored routine replayed its steps instead of rebuilding non-step state only"
        )

        delay(40.Ticks)
        assertTrue(restored.recordedStages.contains(GraceEnded), "Restored routine never reached the end of its body")
    }

    @GameTest(maxTicks = 100)
    fun `phase routine advances by completing`(context: TestContext) = context.test {
        val minigame = minigame().start()

        assertEventually(2.Seconds, "Grace phase never advanced once its routine completed") {
            minigame.state == MinigameState.Playing(TestMinigamePhase.Active)
        }
        assertTrue(
            minigame.recordedStages.contains(TestMinigameStage.GraceReleased),
            "Completing routine did not unwind its finally"
        )
    }

    @GameTest(maxTicks = 100)
    fun `restored routine still advances on completion`(context: TestContext) = context.test {
        val minigame = minigame().start()
        delay(5.Ticks)

        val restored = reload(minigame)

        assertEventually(2.Seconds, "Restored routine did not advance the phase when it completed") {
            restored.state == MinigameState.Playing(TestMinigamePhase.Active)
        }
    }

    @GameTest
    fun `component state and initialization survive a round trip`(context: TestContext) = context.test {
        val component = TestScoreComponent()
        val minigame = minigame().component(component).start()
        component.hits = 7

        val restored = reload(minigame)

        val restoredComponent = assertNotNull(
            restored.components.get(TestScoreComponent.TYPE),
            "Component was not reattached from its data file"
        )
        assertEquals(7, restoredComponent.hits, "Component's persisted state was not restored")
        assertTrue(restoredComponent.initialized, "Component was restored but never initialized")
    }

    @GameTest
    fun `levels survive a round trip`(context: TestContext) = context.test {
        val minigame = minigame().withLevel().start()
        val dimension = minigame.levels.require(TestMinigame.LEVEL).dimension()

        val restored = reload(minigame)

        val level = assertNotNull(restored.levels.get(TestMinigame.LEVEL), "Level was not restored")
        assertEquals(dimension, level.dimension(), "Restored level resolved to a different dimension")
        assertEquals(
            LevelOwnership.Owned,
            restored.levels.ownership(level),
            "Restored level did not keep the ownership it was saved with"
        )

        restored.close()
        server.deleteCustomLevel(level as CustomLevel)
    }

    @GameTest
    fun `minigame cannot be loaded twice`(context: TestContext) = context.test {
        val minigame = minigame().start()
        minigame.save().join()

        assertThrows<MinigameCreationException>("Reading a live minigame's save worked") {
            Minigames.read(minigame.getSavePath(), server)
        }
        assertEquals(minigame, Minigames.get(minigame.uuid), "The live minigame was displaced by the read")
    }

    @GameTest
    fun `minigame saves happen in order`(context: TestContext) = context.test {
        val minigame = minigame().score(1).start()

        val first = minigame.save()
        minigame.score = 2
        val second = minigame.save()
        first.join()
        second.join()

        val restored = reload(minigame)
        assertEquals(2, restored.score, "An earlier write overwrote a later one")
    }
}
