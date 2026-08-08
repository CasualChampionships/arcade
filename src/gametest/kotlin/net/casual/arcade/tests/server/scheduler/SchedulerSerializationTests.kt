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

object SchedulerSerializationTests: ArcadeTestSuite() {
    @GameTest
    fun suspendedRoutineResumesWhereItLeftOff(context: ArcadeTestContext) = context.test {
        val owner = RoutineOwner()
        val scheduler = SimpleTickedScheduler.server()
        scheduler.schedule(CountingRoutine(times = 3, interval = 5), owner)

        scheduler.tick(2)
        owner.count shouldEqual 1

        val restored = RoutineOwner()
        val loaded = SimpleTickedScheduler.server()
        loaded.load(server, scheduler.save(server), restored)

        assertEquals(0, restored.count, "Routine ran before the restored scheduler was ticked")
        loaded.tick(40)

        assertEquals(2, restored.count, "Routine did not run exactly its remaining steps")
        assertTrue(restored.log.contains("finished"), "Routine never reached the end of its body")
        assertEquals(1, owner.count, "Restored routine kept using the owner it was saved with")
    }

    @GameTest
    fun restoredRoutineKeepsItsRemainingDelay(context: ArcadeTestContext) = context.test {
        val owner = RoutineOwner()
        val scheduler = SimpleTickedScheduler.server()
        scheduler.schedule(CountingRoutine(times = 2, interval = 10), owner)

        // Runs the first step, then suspends for 10 ticks, of which 4 elapse before saving.
        scheduler.tick(5)
        owner.count shouldEqual 1

        val restored = RoutineOwner()
        val loaded = SimpleTickedScheduler.server()
        loaded.load(server, scheduler.save(server), restored)

        loaded.tick(5)
        assertEquals(0, restored.count, "Restored routine resumed before its remaining delay had elapsed")
        loaded.tick()
        assertEquals(1, restored.count, "Restored routine did not resume once its remaining delay had elapsed")
    }

    @GameTest
    fun routineScheduledButNotYetStartedSurvives(context: ArcadeTestContext) = context.test {
        val owner = RoutineOwner()
        val scheduler = SimpleTickedScheduler.server()
        scheduler.schedule(20.Ticks, CountingRoutine(times = 1, interval = 1), owner)

        val restored = RoutineOwner()
        val loaded = SimpleTickedScheduler.server()
        loaded.load(server, scheduler.save(server), restored)

        loaded.tick(40)
        assertEquals(1, restored.count, "Routine which had not started yet was lost on save")
        assertEquals(0, owner.count, "Original owner was used by the restored routine")
    }

    @GameTest
    fun recordedStepValueIsReplayedRatherThanRecomputed(context: ArcadeTestContext) = context.test {
        val owner = RoutineOwner(next = 11)
        val scheduler = SimpleTickedScheduler.server()
        scheduler.schedule(RecordingRoutine(), owner)

        scheduler.tick()

        val restored = RoutineOwner(next = 22)
        val loaded = SimpleTickedScheduler.server()
        loaded.load(server, scheduler.save(server), restored)

        loaded.tick(10)
        assertEquals(11, restored.recorded, "Recorded step was re-run instead of replaying its value")
    }

    @GameTest
    fun nestedRoutineSurvivesRoundTrip(context: ArcadeTestContext) = context.test {
        val owner = RoutineOwner()
        val scheduler = SimpleTickedScheduler.server()
        scheduler.schedule(NestedRoutine(), owner)

        scheduler.tick()
        assertEquals(listOf("outer-start", "inner-start"), owner.log)

        val restored = RoutineOwner()
        val loaded = SimpleTickedScheduler.server()
        loaded.load(server, scheduler.save(server), restored)

        loaded.tick(20)
        assertEquals(listOf("inner-end", "outer-end"), restored.log)
    }

    @GameTest
    fun cancellingRestoredRoutineUnwindsItWithoutReplayingSteps(context: ArcadeTestContext) = context.test {
        val owner = RoutineOwner()
        val scheduler = SimpleTickedScheduler.server()
        scheduler.schedule(CleanupRoutine(duration = 40), owner)

        scheduler.tick()
        assertEquals(listOf("start"), owner.log)

        val restored = RoutineOwner()
        val loaded = SimpleTickedScheduler.server()
        loaded.load(server, scheduler.save(server), restored)

        assertTrue(loaded.cancelAll())
        assertEquals(listOf("cleanup"), restored.log, "Cancelling a restored routine replayed its steps")

        loaded.tick(80)
        assertEquals(listOf("cleanup"), restored.log, "Cancelled routine kept running")
    }

    @GameTest
    fun restoredRoutineRebuildsNonStepState(context: ArcadeTestContext) = context.test {
        val owner = RoutineOwner()
        val scheduler = SimpleTickedScheduler.server()
        scheduler.schedule(HoldingRoutine(duration = 40), owner)

        scheduler.tick(5)
        assertTrue(owner.held, "Routine did not take hold of its state")
        assertEquals(40, owner.remaining, "Routine was not given its full duration when it first suspended")

        val restored = RoutineOwner()
        val loaded = SimpleTickedScheduler.server()
        loaded.load(server, scheduler.save(server), restored)

        assertTrue(restored.held, "Restored routine did not rebuild its state until it resumed")
        assertEquals(35, restored.remaining, "Restored routine was not given its remaining duration")
        assertEquals(listOf("held"), restored.log, "Restored routine ran past its suspension point")

        loaded.tick(40)
        assertFalse(restored.held, "Restored routine never released its state")
        assertEquals(listOf("held", "released"), restored.log)
    }

    @GameTest
    fun cancellingRestoredRoutineReleasesNonStepState(context: ArcadeTestContext) = context.test {
        val owner = RoutineOwner()
        val scheduler = SimpleTickedScheduler.server()
        scheduler.schedule(HoldingRoutine(duration = 40), owner)

        scheduler.tick()

        val restored = RoutineOwner()
        val loaded = SimpleTickedScheduler.server()
        loaded.load(server, scheduler.save(server), restored)

        assertTrue(loaded.cancelAll())
        assertFalse(restored.held, "Cancelling a restored routine did not release its state")
        assertEquals(listOf("held", "released"), restored.log)
    }

    @GameTest
    fun plainTasksAreNotSerialized(context: ArcadeTestContext) = context.test {
        val owner = RoutineOwner()
        val scheduler = SimpleTickedScheduler.server()
        var ran = false
        scheduler.schedule(5.Ticks) { ran = true }
        scheduler.schedule(5.Ticks, CountingRoutine(times = 1, interval = 1), owner)

        val restored = RoutineOwner()
        val loaded = SimpleTickedScheduler.server()
        loaded.load(server, scheduler.save(server), restored)

        loaded.tick(40)
        assertFalse(ran, "A plain task was serialized alongside the routines")
        assertEquals(1, restored.count, "The routine was not serialized")
    }

    @GameTest
    fun routineSavedAtDifferentVersionIsNotRestored(context: ArcadeTestContext) = context.test {
        val owner = RoutineOwner()
        val scheduler = SimpleTickedScheduler.server()
        scheduler.schedule(OutdatedRoutine(version = 1), owner)

        scheduler.tick()
        assertEquals(listOf("first"), owner.log)

        val restored = RoutineOwner()
        val loaded = SimpleTickedScheduler.server()
        loaded.load(server, scheduler.save(server), restored)

        loaded.tick(40)
        assertEquals(emptyList(), restored.log, "Routine saved at another version was replayed anyway")
    }

    @GameTest
    fun routineWhoseBodyChangedIsAborted(context: ArcadeTestContext) = context.test {
        val owner = RoutineOwner()
        val scheduler = SimpleTickedScheduler.server()
        scheduler.schedule(DivergingRoutine(inserted = false), owner)

        scheduler.tick()
        assertEquals(listOf("first"), owner.log)

        val restored = RoutineOwner()
        val loaded = SimpleTickedScheduler.server()
        loaded.load(server, scheduler.save(server), restored)

        loaded.tick(40)
        assertEquals(emptyList<String>(), restored.log, "Routine kept running after its body diverged on replay")
    }

    @GameTest
    fun serializingAnEmptySchedulerRestoresNothing(context: ArcadeTestContext) = context.test {
        val scheduler = SimpleTickedScheduler.server()
        val loaded = SimpleTickedScheduler.server()
        loaded.load(server, scheduler.save(server), RoutineOwner())

        assertFalse(loaded.cancelAll(), "Restoring an empty scheduler scheduled something")
    }
}
