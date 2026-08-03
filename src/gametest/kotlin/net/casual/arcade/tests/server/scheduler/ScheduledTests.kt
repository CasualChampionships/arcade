/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.tests.server.scheduler

import net.casual.arcade.gametest.ArcadeTestContext
import net.casual.arcade.gametest.ArcadeTestSuite
import net.casual.arcade.scheduler.SimpleTickedScheduler
import net.casual.arcade.scheduler.task.Task
import net.casual.arcade.utils.TimeUtils.Ticks
import net.fabricmc.fabric.api.gametest.v1.GameTest

object ScheduledTests: ArcadeTestSuite() {
    @GameTest
    fun cancelledTaskDoesNotRun(context: ArcadeTestContext) = context.test {
        val scheduler = SimpleTickedScheduler.server()
        var ran = false
        val handle = scheduler.schedule(2.Ticks) { ran = true }

        handle.cancel()
        scheduler.tick(10)
        assertFalse(ran, "Cancelled task still ran")
    }

    @GameTest
    fun handleIsNotFinishedUntilItRuns(context: ArcadeTestContext) = context.test {
        val scheduler = SimpleTickedScheduler.server()
        val handle = scheduler.schedule(2.Ticks) { }

        assertFalse(handle.isFinished, "Handle reported finished before its task had run")
        scheduler.tick(2)
        assertFalse(handle.isFinished, "Handle reported finished before its delay had elapsed")
        scheduler.tick()
        assertTrue(handle.isFinished, "Handle did not report finished once its task had run")
    }

    @GameTest
    fun handleIsFinishedOnceCancelled(context: ArcadeTestContext) = context.test {
        val scheduler = SimpleTickedScheduler.server()
        val handle = scheduler.schedule(2.Ticks) { }

        assertFalse(handle.isFinished)
        handle.cancel()
        assertTrue(handle.isFinished, "Handle did not report finished once it was cancelled")
    }

    @GameTest
    fun cancellingAfterItHasRunDoesNotRunItAgain(context: ArcadeTestContext) = context.test {
        val scheduler = SimpleTickedScheduler.server()
        var count = 0
        val handle = scheduler.schedule(2.Ticks) { count += 1 }

        scheduler.tick(10)
        count shouldEqual 1

        handle.cancel()
        assertEquals(1, count, "Cancelling an already-run task ran it again")
    }

    @GameTest
    fun cancellingTwiceIsIdempotent(context: ArcadeTestContext) = context.test {
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
    fun schedulingTheSameTaskTwiceGivesIndependentHandles(context: ArcadeTestContext) = context.test {
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
    fun cancellingOneTaskLeavesTheRestOfItsTickAlone(context: ArcadeTestContext) = context.test {
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
