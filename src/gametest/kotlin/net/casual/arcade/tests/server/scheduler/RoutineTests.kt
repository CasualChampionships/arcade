/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.tests.server.scheduler

import net.casual.arcade.gametest.TestContext
import net.casual.arcade.scheduler.ArcadeScheduler
import net.casual.arcade.tests.server.ArcadeTestSuite
import net.casual.arcade.scheduler.SimpleTickedScheduler
import net.casual.arcade.scheduler.utils.schedule
import net.casual.arcade.utils.TimeUtils.Ticks
import net.fabricmc.fabric.api.gametest.v1.GameTest

@Suppress("FunctionName", "Unused")
object RoutineTests: ArcadeTestSuite() {
    override val namespace: String = ArcadeScheduler.MOD_ID

    @GameTest
    fun `routine starts after scheduler tick`(context: TestContext) = context.test {
        val owner = RoutineOwner()
        val scheduler = SimpleTickedScheduler.server()
        scheduler.schedule(CountingRoutine(times = 1, interval = 1), owner)

        assertEquals(0, owner.count, "Routine ran before the scheduler was ticked")
        scheduler.tick()
        owner.count shouldEqual 1
    }

    @GameTest
    fun `routine starts after delay`(context: TestContext) = context.test {
        val owner = RoutineOwner()
        val scheduler = SimpleTickedScheduler.server()
        scheduler.schedule(5.Ticks, CountingRoutine(times = 1, interval = 1), owner)

        scheduler.tick(5)
        assertEquals(0, owner.count, "Routine started before its initial delay had elapsed")
        scheduler.tick()
        owner.count shouldEqual 1
    }

    @GameTest
    fun `routine runs every step`(context: TestContext) = context.test {
        val owner = RoutineOwner()
        val scheduler = SimpleTickedScheduler.server()
        scheduler.schedule(CountingRoutine(times = 3, interval = 2), owner)

        scheduler.tick(40)
        owner.count shouldEqual 3
        assertEquals(listOf("finished"), owner.log)
    }

    @GameTest
    fun `routine suspends between steps`(context: TestContext) = context.test {
        val owner = RoutineOwner()
        val scheduler = SimpleTickedScheduler.server()
        scheduler.schedule(CountingRoutine(times = 3, interval = 5), owner)

        scheduler.tick()
        assertEquals(1, owner.count, "Routine did not run its first step")
        scheduler.tick(4)
        assertEquals(1, owner.count, "Routine did not suspend between its steps")
    }

    @GameTest
    fun `routine reports when finished`(context: TestContext) = context.test {
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
    fun `routine step returns block return value`(context: TestContext) = context.test {
        val owner = RoutineOwner(next = 7)
        val scheduler = SimpleTickedScheduler.server()
        scheduler.schedule(RecordingRoutine(), owner)

        scheduler.tick(10)
        owner.recorded shouldEqual 7
    }

    @GameTest
    fun `nested routine suspends correctly`(context: TestContext) = context.test {
        val owner = RoutineOwner()
        val scheduler = SimpleTickedScheduler.server()
        scheduler.schedule(NestedRoutine(), owner)

        scheduler.tick()
        assertEquals(listOf("outer-start", "inner-start"), owner.log)
        scheduler.tick(10)
        assertEquals(listOf("outer-start", "inner-start", "inner-end", "outer-end"), owner.log)
    }

    @GameTest
    fun `routine unwinds when cancelled`(context: TestContext) = context.test {
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
    fun `routine only unwinds once`(context: TestContext) = context.test {
        val owner = RoutineOwner()
        val scheduler = SimpleTickedScheduler.server()
        val handle = scheduler.schedule(CleanupRoutine(duration = 40), owner)

        scheduler.tick()
        handle.cancel()
        handle.cancel()
        assertEquals(listOf("start", "cleanup"), owner.log)
    }

    @GameTest
    fun `cancelled routine doesnt run`(context: TestContext) = context.test {
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
    fun `routines unwind when scheduler cancelled`(context: TestContext) = context.test {
        val owner = RoutineOwner()
        val scheduler = SimpleTickedScheduler.server()
        scheduler.schedule(CleanupRoutine(duration = 40), owner)

        scheduler.tick()
        assertTrue(scheduler.cancelAll())
        assertEquals(listOf("start", "cleanup"), owner.log)
    }

    @GameTest
    fun `routines dont unwind when scheduler cleared`(context: TestContext) = context.test {
        val owner = RoutineOwner()
        val scheduler = SimpleTickedScheduler.server()
        scheduler.schedule(CleanupRoutine(duration = 40), owner)

        scheduler.tick()
        assertTrue(scheduler.clear())
        scheduler.tick(80)
        assertEquals(listOf("start"), owner.log, "clear unwound the routine instead of discarding it")
    }

    @GameTest
    fun `unregistered routine is not scheduled`(context: TestContext) = context.test {
        val scheduler = SimpleTickedScheduler.server()
        assertThrows<IllegalArgumentException> {
            scheduler.schedule(UnregisteredRoutine(), RoutineOwner())
        }
    }

    @GameTest
    fun `routine owner is persistent`(context: TestContext) = context.test {
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
