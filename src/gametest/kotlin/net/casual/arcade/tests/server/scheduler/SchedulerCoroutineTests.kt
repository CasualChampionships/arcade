/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.tests.server.scheduler

import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import net.casual.arcade.gametest.TestContext
import net.casual.arcade.scheduler.ArcadeScheduler
import net.casual.arcade.tests.server.ArcadeTestSuite
import net.casual.arcade.scheduler.SimpleTickedScheduler
import net.casual.arcade.scheduler.utils.asCoroutineDispatcher
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.coroutine.delay
import net.casual.arcade.utils.coroutine.getCoroutineScope
import net.fabricmc.fabric.api.gametest.v1.GameTest

@Suppress("FunctionName", "Unused")
object SchedulerCoroutineTests: ArcadeTestSuite() {
    override val namespace: String = ArcadeScheduler.MOD_ID

    @GameTest
    fun `dispatcher runs inline on main thread`(context: TestContext) = context.test {
        val scheduler = SimpleTickedScheduler.server()
        var started = false
        server.getCoroutineScope().launch(scheduler.asCoroutineDispatcher()) {
            started = true
        }

        assertTrue(started, "Coroutine was not started inline when already on the server thread")
    }

    @GameTest
    fun `delay resumes after correct delay`(context: TestContext) = context.test {
        val scheduler = SimpleTickedScheduler.server()
        var resumed = false
        val job = server.getCoroutineScope().launch(scheduler.asCoroutineDispatcher()) {
            delay(3.Ticks)
            resumed = true
        }

        scheduler.tick(3)
        assertFalse(resumed, "Coroutine resumed before its delay had elapsed")
        scheduler.tick()
        assertTrue(resumed, "Coroutine did not resume after its delay had elapsed")
        assertTrue(job.isCompleted)
    }

    @GameTest
    fun `delay not affected by other schedulers`(context: TestContext) = context.test {
        val scheduler = SimpleTickedScheduler.server()
        val other = SimpleTickedScheduler.server()
        var resumed = false
        server.getCoroutineScope().launch(scheduler.asCoroutineDispatcher()) {
            delay(2.Ticks)
            resumed = true
        }

        other.tick(20)
        assertFalse(resumed, "Coroutine resumed from an unrelated scheduler's ticks")
        scheduler.tick(3)
        assertTrue(resumed, "Coroutine did not resume from its own scheduler's ticks")
    }

    @GameTest
    fun `cancelling job prevents resuming`(context: TestContext) = context.test {
        val scheduler = SimpleTickedScheduler.server()
        var resumed = false
        val job = server.getCoroutineScope().launch(scheduler.asCoroutineDispatcher()) {
            delay(5.Ticks)
            resumed = true
        }

        job.cancel()
        scheduler.tick(20)
        assertFalse(resumed, "Cancelled coroutine resumed anyway")
    }

    @GameTest
    fun `cancelling scheduler cancels coroutines`(context: TestContext) = context.test {
        val scheduler = SimpleTickedScheduler.server()
        var resumed = false
        var cleanedUp = false
        val job = server.getCoroutineScope().launch(scheduler.asCoroutineDispatcher()) {
            try {
                delay(20.Ticks)
                resumed = true
            } finally {
                cleanedUp = true
            }
        }

        scheduler.cancelAll()
        scheduler.tick(40)
        assertFalse(resumed, "Coroutine resumed after its scheduler cancelled everything")
        assertTrue(cleanedUp, "Coroutine was never unwound when its scheduler cancelled everything")
        assertTrue(job.isCancelled)
    }

    @GameTest
    fun `sequenced delays all resume`(context: TestContext) = context.test {
        val scheduler = SimpleTickedScheduler.server()
        val reached = ArrayList<Int>()
        server.getCoroutineScope().launch(scheduler.asCoroutineDispatcher()) {
            reached.add(0)
            delay(1.Ticks)
            reached.add(1)
            delay(1.Ticks)
            reached.add(2)
        }

        assertEquals(listOf(0), reached)
        scheduler.tick()
        assertEquals(listOf(0), reached, "Coroutine resumed before its delay had elapsed")
        scheduler.tick()
        assertEquals(listOf(0, 1), reached)
        scheduler.tick()
        assertEquals(listOf(0, 1, 2), reached)
    }

    @GameTest
    fun `scheduler scope is constant`(context: TestContext) = context.test {
        val scheduler = SimpleTickedScheduler.server()
        assertTrue(
            scheduler.asCoroutineScope() === scheduler.asCoroutineScope(),
            "Scheduler handed out a different scope each time it was asked"
        )
    }

    @GameTest
    fun `scheduler scope can loop`(context: TestContext) = context.test {
        val scheduler = SimpleTickedScheduler.server()
        var count = 0
        val job = scheduler.asCoroutineScope().launch {
            while (isActive) {
                count += 1
                delay(5.Ticks)
            }
        }

        assertEquals(1, count, "Loop did not start inline")
        scheduler.tick(5)
        assertEquals(1, count, "Loop resumed before its interval had elapsed")
        scheduler.tick()
        count shouldEqual 2

        // Every subsequent iteration resumes from inside a tick, so it lands
        // exactly `interval` ticks later.
        scheduler.tick(5)
        count shouldEqual 3
        scheduler.tick(5)
        count shouldEqual 4

        job.cancel()
        scheduler.tick(20)
        assertEquals(4, count, "Loop kept running after its job was cancelled")
    }

    @GameTest
    fun `scheduler scope is reusable`(context: TestContext) = context.test {
        val scheduler = SimpleTickedScheduler.server()
        var first = false
        var second = false
        scheduler.asCoroutineScope().launch {
            delay(5.Ticks)
            first = true
        }

        scheduler.cancelAll()

        scheduler.asCoroutineScope().launch {
            delay(5.Ticks)
            second = true
        }
        scheduler.tick(10)

        assertFalse(first, "Coroutine survived cancelAll")
        assertTrue(second, "cancelAll permanently cancelled the scheduler's scope")
    }
}
