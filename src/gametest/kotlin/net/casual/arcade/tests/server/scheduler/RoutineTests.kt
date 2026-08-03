/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.tests.server.scheduler

import net.casual.arcade.gametest.ArcadeTestContext
import net.casual.arcade.gametest.ArcadeTestSuite
import net.casual.arcade.scheduler.SimpleTickedScheduler
import net.casual.arcade.scheduler.utils.schedule
import net.casual.arcade.utils.TimeUtils.Ticks
import net.fabricmc.fabric.api.gametest.v1.GameTest

object RoutineTests: ArcadeTestSuite() {
    @GameTest
    fun routineDoesNotStartUntilTheSchedulerIsTicked(context: ArcadeTestContext) = context.test {
        val owner = RoutineOwner()
        val scheduler = SimpleTickedScheduler.server()
        scheduler.schedule(CountingRoutine(times = 1, interval = 1), owner)

        assertEquals(0, owner.count, "Routine ran before the scheduler was ticked")
        scheduler.tick()
        owner.count shouldEqual 1
    }

    @GameTest
    fun routineStartsAfterInitialDelay(context: ArcadeTestContext) = context.test {
        val owner = RoutineOwner()
        val scheduler = SimpleTickedScheduler.server()
        scheduler.schedule(5.Ticks, CountingRoutine(times = 1, interval = 1), owner)

        scheduler.tick(5)
        assertEquals(0, owner.count, "Routine started before its initial delay had elapsed")
        scheduler.tick()
        owner.count shouldEqual 1
    }

    @GameTest
    fun routineRunsEveryStep(context: ArcadeTestContext) = context.test {
        val owner = RoutineOwner()
        val scheduler = SimpleTickedScheduler.server()
        scheduler.schedule(CountingRoutine(times = 3, interval = 2), owner)

        scheduler.tick(40)
        owner.count shouldEqual 3
        assertEquals(listOf("finished"), owner.log)
    }

    @GameTest
    fun routineSuspendsBetweenItsSteps(context: ArcadeTestContext) = context.test {
        val owner = RoutineOwner()
        val scheduler = SimpleTickedScheduler.server()
        scheduler.schedule(CountingRoutine(times = 3, interval = 5), owner)

        scheduler.tick()
        assertEquals(1, owner.count, "Routine did not run its first step")
        scheduler.tick(4)
        assertEquals(1, owner.count, "Routine did not suspend between its steps")
    }

    @GameTest
    fun routineHandleReportsWhenItHasFinished(context: ArcadeTestContext) = context.test {
        val owner = RoutineOwner()
        val scheduler = SimpleTickedScheduler.server()
        val handle = scheduler.schedule(CountingRoutine(times = 2, interval = 2), owner)

        assertFalse(handle.isFinished, "Routine reported finished before it started")
        scheduler.tick()
        assertFalse(handle.isFinished, "Routine reported finished while suspended")
        scheduler.tick(40)
        assertTrue(handle.isFinished, "Routine did not report finished once its body returned")
    }

    @GameTest
    fun stepReturnsTheValueItsBlockProduced(context: ArcadeTestContext) = context.test {
        val owner = RoutineOwner(next = 7)
        val scheduler = SimpleTickedScheduler.server()
        scheduler.schedule(RecordingRoutine(), owner)

        scheduler.tick(10)
        owner.recorded shouldEqual 7
    }

    @GameTest
    fun nestedRoutineSharesTheOuterRoutinesSuspensionPoints(context: ArcadeTestContext) = context.test {
        val owner = RoutineOwner()
        val scheduler = SimpleTickedScheduler.server()
        scheduler.schedule(NestedRoutine(), owner)

        scheduler.tick()
        assertEquals(listOf("outer-start", "inner-start"), owner.log)
        scheduler.tick(10)
        assertEquals(listOf("outer-start", "inner-start", "inner-end", "outer-end"), owner.log)
    }

    @GameTest
    fun cancellingRunningRoutineUnwindsIt(context: ArcadeTestContext) = context.test {
        val owner = RoutineOwner()
        val scheduler = SimpleTickedScheduler.server()
        val handle = scheduler.schedule(CleanupRoutine(duration = 40), owner)

        scheduler.tick()
        assertEquals(listOf("start"), owner.log)

        handle.cancel()
        assertEquals(listOf("start", "cleanup"), owner.log)
        assertTrue(handle.isFinished)

        scheduler.tick(60)
        assertEquals(listOf("start", "cleanup"), owner.log, "Cancelled routine kept running")
    }

    @GameTest
    fun cancellingRoutineTwiceOnlyUnwindsItOnce(context: ArcadeTestContext) = context.test {
        val owner = RoutineOwner()
        val scheduler = SimpleTickedScheduler.server()
        val handle = scheduler.schedule(CleanupRoutine(duration = 40), owner)

        scheduler.tick()
        handle.cancel()
        handle.cancel()
        assertEquals(listOf("start", "cleanup"), owner.log)
    }

    @GameTest
    fun cancellingRoutineBeforeItStartsRunsNothing(context: ArcadeTestContext) = context.test {
        val owner = RoutineOwner()
        val scheduler = SimpleTickedScheduler.server()
        val handle = scheduler.schedule(20.Ticks, CleanupRoutine(duration = 40), owner)

        handle.cancel()
        assertTrue(handle.isFinished)
        assertEquals(emptyList(), owner.log, "Cancelling an unstarted routine ran part of its body")

        scheduler.tick(80)
        assertEquals(emptyList(), owner.log, "Cancelled routine ran anyway")
    }

    @GameTest
    fun cancelAllUnwindsRoutines(context: ArcadeTestContext) = context.test {
        val owner = RoutineOwner()
        val scheduler = SimpleTickedScheduler.server()
        scheduler.schedule(CleanupRoutine(duration = 40), owner)

        scheduler.tick()
        assertTrue(scheduler.cancelAll())
        assertEquals(listOf("start", "cleanup"), owner.log)
    }

    @GameTest
    fun clearDiscardsRoutinesWithoutUnwindingThem(context: ArcadeTestContext) = context.test {
        val owner = RoutineOwner()
        val scheduler = SimpleTickedScheduler.server()
        scheduler.schedule(CleanupRoutine(duration = 40), owner)

        scheduler.tick()
        assertTrue(scheduler.clear())
        scheduler.tick(80)
        assertEquals(listOf("start"), owner.log, "clear unwound the routine instead of discarding it")
    }

    @GameTest
    fun unregisteredRoutineCannotBeScheduled(context: ArcadeTestContext) = context.test {
        val scheduler = SimpleTickedScheduler.server()
        assertThrows<IllegalArgumentException> {
            scheduler.schedule(UnregisteredRoutine(), RoutineOwner())
        }
    }

    @GameTest
    fun routineOwnerIsTheOneItWasScheduledWith(context: ArcadeTestContext) = context.test {
        val first = RoutineOwner()
        val second = RoutineOwner()
        val scheduler = SimpleTickedScheduler.server()
        scheduler.schedule(CountingRoutine(times = 1, interval = 1), first)
        scheduler.schedule(CountingRoutine(times = 1, interval = 1), second)

        scheduler.tick(10)
        first.count shouldEqual 1
        second.count shouldEqual 1
    }
}
