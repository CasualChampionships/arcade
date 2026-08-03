/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.tests.server.scheduler

import net.casual.arcade.gametest.ArcadeTestContext
import net.casual.arcade.gametest.ArcadeTestSuite
import net.casual.arcade.scheduler.SimpleTickedScheduler
import net.casual.arcade.scheduler.TickedScheduler.Companion.schedule
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.side.LogicalSide
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.fabricmc.fabric.api.gametest.v1.GameTest

object SimpleTickedSchedulerTests: ArcadeTestSuite() {
    @GameTest
    fun schedulerReportsItsSide(context: ArcadeTestContext) = context.test {
        SimpleTickedScheduler.server().target shouldEqual LogicalSide.Server
        SimpleTickedScheduler.client().target shouldEqual LogicalSide.Client
    }

    @GameTest
    fun taskDoesNotRunUntilScheduledIsTicked(context: ArcadeTestContext) = context.test {
        val scheduler = SimpleTickedScheduler.server()
        var ran = false
        scheduler.schedule(MinecraftTimeDuration.ZERO) { ran = true }

        assertFalse(ran, "Task ran before the scheduler was ticked")
        scheduler.tick()
        assertTrue(ran, "Task did not run when the scheduler was ticked")
    }

    @GameTest
    fun taskRunsOnlyOnce(context: ArcadeTestContext) = context.test {
        val scheduler = SimpleTickedScheduler.server()
        var count = 0
        scheduler.schedule(MinecraftTimeDuration.ZERO) { count += 1 }

        scheduler.tick(10)
        count shouldEqual 1
    }

    @GameTest
    fun taskRunsAfterItsDelay(context: ArcadeTestContext) = context.test {
        val scheduler = SimpleTickedScheduler.server()
        var ran = false
        scheduler.schedule(3.Ticks) { ran = true }

        scheduler.tick(3)
        assertFalse(ran, "Task ran before its delay had elapsed")
        scheduler.tick()
        assertTrue(ran, "Task did not run after its delay had elapsed")
    }

    @GameTest
    fun tasksRunInTheOrderTheyWereScheduled(context: ArcadeTestContext) = context.test {
        val scheduler = SimpleTickedScheduler.server()
        val order = ArrayList<Int>()
        scheduler.schedule(1.Ticks) { order.add(1) }
        scheduler.schedule(1.Ticks) { order.add(2) }
        scheduler.schedule(1.Ticks) { order.add(3) }

        scheduler.tick(2)
        assertEquals(listOf(1, 2, 3), order)
    }

    @GameTest
    fun taskScheduledDuringTickRunsOnLaterTick(context: ArcadeTestContext) = context.test {
        val scheduler = SimpleTickedScheduler.server()
        var nested = false
        scheduler.schedule(MinecraftTimeDuration.ZERO) {
            scheduler.schedule(MinecraftTimeDuration.ZERO) { nested = true }
        }

        scheduler.tick()
        assertFalse(nested, "Nested task ran within the tick it was scheduled from")
        scheduler.tick()
        assertTrue(nested, "Nested task did not run on the following tick")
    }

    @GameTest
    fun delayMeansTheSameFromInsideATickAsFromOutside(context: ArcadeTestContext) = context.test {
        val scheduler = SimpleTickedScheduler.server()
        var outer = 0
        var inner = 0

        // Scheduled from outside a tick, so it lands on the 4th tick.
        scheduler.schedule(3.Ticks) { outer += 1 }
        // Scheduled from *inside* the first tick, so it lands 3 ticks after
        // that one, which is also the 4th tick.
        scheduler.schedule(MinecraftTimeDuration.ZERO) {
            scheduler.schedule(3.Ticks) { inner += 1 }
        }

        scheduler.tick(3)
        assertEquals(0, outer, "Task scheduled from outside a tick ran early")
        assertEquals(0, inner, "Task scheduled from inside a tick ran early")

        scheduler.tick()
        assertEquals(1, outer, "Task scheduled from outside a tick did not run after its delay")
        assertEquals(1, inner, "The same delay meant a different number of ticks from inside a tick")
    }

    @GameTest
    fun zeroDelayFromInsideATickRunsOnTheNextTick(context: ArcadeTestContext) = context.test {
        val scheduler = SimpleTickedScheduler.server()
        var ran = false
        scheduler.schedule(MinecraftTimeDuration.ZERO) {
            scheduler.schedule(MinecraftTimeDuration.ZERO) { ran = true }
        }

        scheduler.tick()
        assertFalse(ran, "Zero delay ran within the tick which had already been drained")
        scheduler.tick()
        assertTrue(ran, "Zero delay did not fall through to the next tick")
    }

    @GameTest
    fun throwingTaskDoesNotPreventOtherTasks(context: ArcadeTestContext) = context.test {
        val scheduler = SimpleTickedScheduler.server()
        var ran = false
        scheduler.schedule(MinecraftTimeDuration.ZERO) {
            throw IllegalStateException("Intentionally thrown by throwingTaskDoesNotPreventOtherTasks")
        }
        scheduler.schedule(MinecraftTimeDuration.ZERO) { ran = true }

        scheduler.tick()
        assertTrue(ran, "Task scheduled after a throwing task did not run")

        var later = false
        scheduler.schedule(MinecraftTimeDuration.ZERO) { later = true }
        scheduler.tick()
        assertTrue(later, "Scheduler stopped running tasks after one threw")
    }

    @GameTest
    fun intOverloadSchedulesInTicks(context: ArcadeTestContext) = context.test {
        val scheduler = SimpleTickedScheduler.server()
        var ran = false
        scheduler.schedule(2) { ran = true }

        scheduler.tick(2)
        assertFalse(ran, "Task ran before its delay had elapsed")
        scheduler.tick()
        assertTrue(ran, "Task did not run after its delay had elapsed")
    }

    @GameTest
    fun cancelRemovesTasksAtTheGivenDelta(context: ArcadeTestContext) = context.test {
        val scheduler = SimpleTickedScheduler.server()
        var cancelled = false
        var kept = false
        scheduler.schedule(2.Ticks) { cancelled = true }
        scheduler.schedule(3.Ticks) { kept = true }

        scheduler.cancel(2)
        scheduler.tick(10)
        assertFalse(cancelled, "Task at the cancelled delta still ran")
        assertTrue(kept, "Task at a different delta was cancelled too")
    }

    @GameTest
    fun cancelAllRemovesEveryTask(context: ArcadeTestContext) = context.test {
        val scheduler = SimpleTickedScheduler.server()
        var count = 0
        scheduler.schedule(MinecraftTimeDuration.ZERO) { count += 1 }
        scheduler.schedule(5.Ticks) { count += 1 }
        scheduler.schedule(20.Ticks) { count += 1 }

        assertTrue(scheduler.cancelAll(), "cancelAll reported nothing to cancel")
        scheduler.tick(40)
        assertEquals(0, count, "Cancelled tasks still ran")
        assertFalse(scheduler.cancelAll(), "cancelAll reported work on an empty scheduler")
    }

    @GameTest
    fun cancelAllFinishesEveryHandle(context: ArcadeTestContext) = context.test {
        val scheduler = SimpleTickedScheduler.server()
        val handle = scheduler.schedule(20.Ticks) { }

        assertTrue(scheduler.cancelAll())
        assertTrue(handle.isFinished, "cancelAll did not cancel the task's handle")
    }

    @GameTest
    fun clearRemovesEveryTaskWithoutCancellingIt(context: ArcadeTestContext) = context.test {
        val scheduler = SimpleTickedScheduler.server()
        var ran = false
        val handle = scheduler.schedule(5.Ticks) { ran = true }

        assertTrue(scheduler.clear(), "clear reported nothing to clear")
        scheduler.tick(40)
        assertFalse(ran, "Cleared task still ran")
        assertFalse(handle.isFinished, "clear cancelled the task instead of discarding it")
        assertFalse(scheduler.clear(), "clear reported work on an empty scheduler")
    }
}
