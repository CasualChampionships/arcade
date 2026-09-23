/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.tests.server.minigame

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.gametest.TestContext
import net.casual.arcade.minigame.phase.MinigamePhaseLifetime
import net.casual.arcade.tests.server.ArcadeTestSuite
import net.casual.arcade.tests.server.minigame.utils.TestMinigameEvent
import net.casual.arcade.tests.server.minigame.utils.TestMinigamePhase.Active
import net.casual.arcade.tests.server.minigame.utils.TestMinigamePhase.Grace
import net.casual.arcade.tests.server.minigame.utils.TestMinigamePhase.Over
import net.casual.arcade.tests.server.minigame.utils.minigame
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.arcade
import net.casual.arcade.utils.coroutine.delay
import net.fabricmc.fabric.api.gametest.v1.GameTest

@Suppress("FunctionName", "Unused")
object MinigameScopeLifetimeTests: ArcadeTestSuite() {
    override val namespace: String = ArcadeUtils.MOD_ID

    @GameTest
    fun `forever scope survives all phase changes`(context: TestContext) = context.test {
        val minigame = minigame().withoutPhaseLogic().phase(Grace).start()
        val scope = minigame.scopes.create(MinigamePhaseLifetime.Forever)

        minigame.phases.set(Active)
        assertFalse(scope.closed, "Forever scope closed on a forward transition")

        minigame.phases.set(Grace)
        assertFalse(scope.closed, "Forever scope closed on a rewind")

        minigame.phases.set(Grace, force = true)
        assertFalse(scope.closed, "Forever scope closed on a forced restart")
    }

    @GameTest
    fun `current scope closes on phase change`(context: TestContext) = context.test {
        val minigame = minigame().withoutPhaseLogic().phase(Grace).start()
        val forward = minigame.scopes.create(MinigamePhaseLifetime.Current)

        minigame.phases.set(Active)
        assertTrue(forward.closed, "Current scope survived a forward transition")

        val rewind = minigame.scopes.create(MinigamePhaseLifetime.Current)
        minigame.phases.set(Grace)
        assertTrue(rewind.closed, "Current scope survived a rewind")
    }

    @GameTest
    fun `current scope closes on a forced phase change`(context: TestContext) = context.test {
        val minigame = minigame().withoutPhaseLogic().phase(Grace).start()
        val scope = minigame.scopes.create(MinigamePhaseLifetime.Current)

        minigame.phases.set(Grace, force = true)

        assertTrue(scope.closed, "Restarting a phase did not cancel that phase's own work")
    }

    @GameTest
    fun `forward scope survives a forward phase change`(context: TestContext) = context.test {
        val minigame = minigame().withoutPhaseLogic().phase(Grace).start()
        val scope = minigame.scopes.create(MinigamePhaseLifetime.Forward)

        minigame.phases.set(Active)

        assertFalse(scope.closed, "Forward scope closed on a forward transition")
    }

    @GameTest
    fun `forward scope closes on a rewind`(context: TestContext) = context.test {
        val minigame = minigame().withoutPhaseLogic().phase(Active).start()
        val scope = minigame.scopes.create(MinigamePhaseLifetime.Forward)

        minigame.phases.set(Grace)

        assertTrue(scope.closed, "Forward scope survived an admin rewind")
    }

    @GameTest
    fun `forward scope closes on a forced phase change`(context: TestContext) = context.test {
        val minigame = minigame().withoutPhaseLogic().phase(Grace).start()
        val scope = minigame.scopes.create(MinigamePhaseLifetime.Forward)

        minigame.phases.set(Grace, force = true)

        assertTrue(scope.closed, "Forward scope survived a forced restart of its own phase")
    }

    @GameTest
    fun `until scope survives before its bound phase`(context: TestContext) = context.test {
        val minigame = minigame().withoutPhaseLogic().phase(Grace).start()
        val scope = minigame.scopes.create(MinigamePhaseLifetime.Until(Over))

        minigame.phases.set(Active)

        assertFalse(scope.closed, "Until scope closed before reaching its bound")
    }

    @GameTest
    fun `until scope closes at its bound phase`(context: TestContext) = context.test {
        val minigame = minigame().withoutPhaseLogic().phase(Grace).start()
        val scope = minigame.scopes.create(MinigamePhaseLifetime.Until(Active))

        minigame.phases.set(Active)

        assertTrue(scope.closed, "Until scope survived its own bound")
    }

    @GameTest
    fun `until scope survives a forced phase change`(context: TestContext) = context.test {
        val minigame = minigame().withoutPhaseLogic().phase(Grace).start()
        val scope = minigame.scopes.create(MinigamePhaseLifetime.Until(Over))

        minigame.phases.set(Grace, force = true)

        assertFalse(scope.closed, "Until scope treated a forced restart as leaving its bound")
    }

    @GameTest
    fun `during scope survives a listed phase`(context: TestContext) = context.test {
        val minigame = minigame().withoutPhaseLogic().phase(Grace).start()
        val scope = minigame.scopes.create(MinigamePhaseLifetime.During(setOf(Grace, Active)))

        minigame.phases.set(Active)

        assertFalse(scope.closed, "During scope closed on a phase it lists")
    }

    @GameTest
    fun `during scope closes on an unlisted phase`(context: TestContext) = context.test {
        val minigame = minigame().withoutPhaseLogic().phase(Grace).start()
        val scope = minigame.scopes.create(MinigamePhaseLifetime.During(setOf(Grace, Active)))

        minigame.phases.set(Over)

        assertTrue(scope.closed, "During scope survived a phase it does not list")
    }

    @GameTest
    fun `during scope survives a forced phase change`(context: TestContext) = context.test {
        val minigame = minigame().withoutPhaseLogic().phase(Grace).start()
        val scope = minigame.scopes.create(MinigamePhaseLifetime.During(setOf(Grace)))

        minigame.phases.set(Grace, force = true)

        assertFalse(scope.closed, "During scope treated a forced restart as leaving its phases")
    }

    @GameTest
    fun `between scope survives inside its bounds`(context: TestContext) = context.test {
        val minigame = minigame().withoutPhaseLogic().phase(Grace).start()
        val scope = minigame.scopes.create(MinigamePhaseLifetime.Between(Grace, Over))

        minigame.phases.set(Active)

        assertFalse(scope.closed, "Between scope closed on a phase inside its bounds")
    }

    @GameTest
    fun `between scope closes at its upper bound`(context: TestContext) = context.test {
        val minigame = minigame().withoutPhaseLogic().phase(Grace).start()
        val scope = minigame.scopes.create(MinigamePhaseLifetime.Between(Grace, Over))

        minigame.phases.set(Over)

        assertTrue(scope.closed, "Between scope survived its upper bound")
    }

    @GameTest
    fun `between scope closes at its lower bound`(context: TestContext) = context.test {
        val minigame = minigame().withoutPhaseLogic().phase(Active).start()
        val scope = minigame.scopes.create(MinigamePhaseLifetime.Between(Grace, Over))

        minigame.phases.set(Grace)

        assertTrue(scope.closed, "Between scope survived a rewind to its lower bound")
    }

    @GameTest
    fun `between scope survives a forced phase change`(context: TestContext) = context.test {
        val minigame = minigame().withoutPhaseLogic().phase(Active).start()
        val scope = minigame.scopes.create(MinigamePhaseLifetime.Between(Grace, Over))

        minigame.phases.set(Active, force = true)

        assertFalse(scope.closed, "Between scope treated a forced restart as leaving its bounds")
    }

    @GameTest(maxTicks = 60)
    fun `closing a scope cancels its tasks`(context: TestContext) = context.test {
        val minigame = minigame().withoutPhaseLogic().phase(Grace).start()
        val scope = minigame.scopes.create(MinigamePhaseLifetime.Current)

        var ran = false
        val task = scope.schedule(5.Ticks) { ran = true }

        minigame.phases.set(Active)

        assertTrue(task.isFinished, "Task in a closed scope was left schedulable")
        delay(20.Ticks)
        assertFalse(ran, "Task in a closed scope still ran")
    }

    @GameTest
    fun `closing a scope unregisters its listeners`(context: TestContext) = context.test {
        val minigame = minigame().withoutPhaseLogic().phase(Grace).start()
        val scope = minigame.scopes.create(MinigamePhaseLifetime.Current)

        var received = 0
        scope.register<TestMinigameEvent> { received += 1 }

        GlobalEventHandler.Server.broadcast(TestMinigameEvent())
        assertEquals(1, received, "Scope listener did not receive an event while the scope was open")

        minigame.phases.set(Active)

        GlobalEventHandler.Server.broadcast(TestMinigameEvent())
        assertEquals(1, received, "Scope listener still fired after its scope closed")
    }

    @GameTest(maxTicks = 60)
    fun `scheduling into a closed scope is rejected`(context: TestContext) = context.test {
        val minigame = minigame().withoutPhaseLogic().phase(Grace).start()
        val scope = minigame.scopes.create(MinigamePhaseLifetime.Current)

        minigame.phases.set(Active)

        var ran = false
        val task = scope.schedule(1.Ticks) { ran = true }

        assertTrue(task.isFinished, "Scheduling into a closed scope returned a live task")
        delay(20.Ticks)
        assertFalse(ran, "Task scheduled into a closed scope ran anyway")
    }

    @GameTest(maxTicks = 60)
    fun `current scope is reset on phase change`(context: TestContext) = context.test {
        val minigame = minigame().withoutPhaseLogic().phase(Grace).start()
        val current = minigame.scopes.current

        var ran = false
        var received = 0
        current.schedule(5.Ticks) { ran = true }
        current.register<TestMinigameEvent> { received += 1 }

        minigame.phases.set(Active)

        assertFalse(current.closed, "The current scope closed on a phase transition")
        assertTrue(current === minigame.scopes.current, "The current scope was replaced on a phase transition")

        GlobalEventHandler.Server.broadcast(TestMinigameEvent())
        delay(20.Ticks)
        assertFalse(ran, "Task in the current scope ran after the phase changed")
        assertEquals(0, received, "Listener in the current scope fired after the phase changed")

        var reran = false
        current.schedule(5.Ticks) { reran = true }
        delay(20.Ticks)
        assertTrue(reran, "Task scheduled into the current scope after a phase change did not run")
    }

    @GameTest
    fun `root and current scopes cannot be closed manually`(context: TestContext) = context.test {
        val minigame = minigame().withoutPhaseLogic().phase(Grace).start()

        minigame.scopes.root.close()
        minigame.scopes.current.close()

        assertFalse(minigame.scopes.root.closed, "The root scope was closed manually")
        assertFalse(minigame.scopes.current.closed, "The current scope was closed manually")
    }

    @GameTest
    fun `named scope is shared by id`(context: TestContext) = context.test {
        val minigame = minigame().withoutPhaseLogic().phase(Grace).start()

        val first = minigame.scopes.named(NAMED, MinigamePhaseLifetime.Forward)
        val second = minigame.scopes.named(NAMED, MinigamePhaseLifetime.Forward)

        assertTrue(first === second, "Getting a named scope twice created two scopes")
        assertEquals(NAMED, first.id)
    }

    @GameTest
    fun `named scope rejects a different lifetime`(context: TestContext) = context.test {
        val minigame = minigame().withoutPhaseLogic().phase(Grace).start()
        minigame.scopes.named(NAMED, MinigamePhaseLifetime.Forward)

        assertThrows<IllegalArgumentException>("Named scope was fetched with a conflicting lifetime") {
            minigame.scopes.named(NAMED, MinigamePhaseLifetime.Current)
        }
    }

    @GameTest(maxTicks = 60)
    fun `named scope is cancelled instead of closed`(context: TestContext) = context.test {
        val minigame = minigame().withoutPhaseLogic().phase(Grace).start()
        val scope = minigame.scopes.named(NAMED, MinigamePhaseLifetime.Current)

        var ran = false
        scope.schedule(5.Ticks) { ran = true }

        minigame.phases.set(Active)

        assertFalse(scope.closed, "Named scope closed when its lifetime ended")
        delay(20.Ticks)
        assertFalse(ran, "Task in a named scope ran after its lifetime ended")

        var reran = false
        scope.schedule(5.Ticks) { reran = true }
        delay(20.Ticks)
        assertTrue(reran, "Task scheduled into a named scope after its lifetime ended did not run")
    }

    @GameTest
    fun `named scope cannot be closed manually`(context: TestContext) = context.test {
        val minigame = minigame().withoutPhaseLogic().phase(Grace).start()
        val scope = minigame.scopes.named(NAMED, MinigamePhaseLifetime.Forever)

        scope.close()

        assertFalse(scope.closed, "Named scope was closed manually")
    }

    @GameTest(maxTicks = 60)
    fun `cancelling a scope keeps it open`(context: TestContext) = context.test {
        val minigame = minigame().withoutPhaseLogic().phase(Grace).start()
        val scope = minigame.scopes.create(MinigamePhaseLifetime.Forever)

        var ran = false
        var received = 0
        scope.schedule(5.Ticks) { ran = true }
        scope.register<TestMinigameEvent> { received += 1 }

        scope.cancel()

        assertFalse(scope.closed, "Cancelling a scope closed it")
        GlobalEventHandler.Server.broadcast(TestMinigameEvent())
        delay(20.Ticks)
        assertFalse(ran, "Task ran after its scope was cancelled")
        assertEquals(0, received, "Listener fired after its scope was cancelled")

        var reran = false
        scope.schedule(5.Ticks) { reran = true }
        delay(20.Ticks)
        assertTrue(reran, "Task scheduled into a cancelled scope did not run")
    }

    @GameTest
    fun `closing the minigame closes every scope`(context: TestContext) = context.test {
        val minigame = minigame().withoutPhaseLogic().phase(Grace).start()
        val scopes = listOf(
            minigame.scopes.create(MinigamePhaseLifetime.Forever),
            minigame.scopes.create(MinigamePhaseLifetime.Current),
            minigame.scopes.create(MinigamePhaseLifetime.Forward),
            minigame.scopes.named(NAMED, MinigamePhaseLifetime.Forever)
        )

        minigame.close()

        for (scope in scopes) {
            assertTrue(scope.closed, "${scope.lifetime} scope outlived its minigame")
        }
        assertTrue(minigame.scopes.root.closed, "The root scope outlived its minigame")
        assertTrue(minigame.scopes.current.closed, "The current scope outlived its minigame")
    }

    private val NAMED = arcade("test_named")
}
