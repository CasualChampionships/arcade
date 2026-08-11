/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.tests.server.scheduler

import net.casual.arcade.gametest.TestContext
import net.casual.arcade.scheduler.ArcadeScheduler
import net.casual.arcade.tests.server.ArcadeTestSuite
import net.casual.arcade.scheduler.SimpleTickedScheduler
import net.casual.arcade.scheduler.task.Task
import net.casual.arcade.utils.TimeUtils.Ticks
import net.fabricmc.fabric.api.gametest.v1.GameTest

@Suppress("FunctionName", "Unused")
object ScheduledTests: ArcadeTestSuite() {
    override val namespace: String = ArcadeScheduler.MOD_ID

    @GameTest
    fun `cancelled task doesnt run`(context: TestContext) = context.test {
        val scheduler = SimpleTickedScheduler.server()
        var ran = false
        val handle = scheduler.schedule(2.Ticks) { ran = true }

        handle.cancel()
        scheduler.tick(10)
        assertFalse(ran, "Cancelled task still ran")
    }

    @GameTest
    fun `task reports when finished`(context: TestContext) = context.test {
        val scheduler = SimpleTickedScheduler.server()
        val handle = scheduler.schedule(2.Ticks) { }

        assertFalse(handle.isFinished, "Handle reported finished before its task had run")
        scheduler.tick(2)
        assertFalse(handle.isFinished, "Handle reported finished before its delay had elapsed")
        scheduler.tick()
        assertTrue(handle.isFinished, "Handle did not report finished once its task had run")
    }

    @GameTest
    fun `task is finished when cancelled`(context: TestContext) = context.test {
        val scheduler = SimpleTickedScheduler.server()
        val handle = scheduler.schedule(2.Ticks) { }

        assertFalse(handle.isFinished)
        handle.cancel()
        assertTrue(handle.isFinished, "Handle did not report finished once it was cancelled")
    }

    @GameTest
    fun `cancelling task twice is idempotent`(context: TestContext) = context.test {
        val scheduler = SimpleTickedScheduler.server()
        var ran = false
        val handle = scheduler.schedule(2.Ticks) { ran = true }

        handle.cancel()
        handle.cancel()
        scheduler.tick(10)
        assertFalse(ran, "Task cancelled twice ran anyway")
        assertTrue(handle.isFinished)
    }

    @GameTest
    fun `scheduling task twice gives unique handles`(context: TestContext) = context.test {
        val scheduler = SimpleTickedScheduler.server()
        var count = 0
        val task = Task { count += 1 }
        val first = scheduler.schedule(2.Ticks, task)
        val second = scheduler.schedule(4.Ticks, task)

        first.cancel()
        scheduler.tick(10)
        assertEquals(1, count, "Cancelling one handle affected the other scheduling of the same task")
        assertTrue(first.isFinished)
        assertTrue(second.isFinished)
    }

    @GameTest
    fun `cancelling task doesnt affect other tasks`(context: TestContext) = context.test {
        val scheduler = SimpleTickedScheduler.server()
        var cancelled = false
        var kept = false
        val handle = scheduler.schedule(2.Ticks) { cancelled = true }
        scheduler.schedule(2.Ticks) { kept = true }

        handle.cancel()
        scheduler.tick(10)
        assertFalse(cancelled, "Cancelled task still ran")
        assertTrue(kept, "Another task scheduled for the same tick was cancelled too")
    }
}
