/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.tests.server.visuals

import net.casual.arcade.gametest.ArcadeTestContext
import net.casual.arcade.gametest.ArcadeTestSuite
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.virtual.visuals.utils.elements.timer.TimerElement
import net.fabricmc.fabric.api.gametest.v1.GameTest
import net.minecraft.network.chat.Component

object TimerElementTests: ArcadeTestSuite() {
    @GameTest
    fun timerCountsDownAsItIsTicked(context: ArcadeTestContext) = context.test {
        val timer = TimerElement(10.Ticks)

        timer.getRemainingDuration() shouldEqual 10.Ticks

        repeat(4) { timer.tick(server) }

        timer.getRemainingDuration() shouldEqual 6.Ticks
        timer.getTotalDuration() shouldEqual 10.Ticks
    }

    @GameTest
    fun progressRunsFromZeroToOne(context: ArcadeTestContext) = context.test {
        val timer = TimerElement(4.Ticks)

        timer.getProgress() shouldEqual 0.0F

        repeat(2) { timer.tick(server) }
        timer.getProgress() shouldEqual 0.5F

        repeat(2) { timer.tick(server) }
        timer.getProgress() shouldEqual 1.0F
    }

    @GameTest
    fun timerCompletesOnceTheDurationHasElapsed(context: ArcadeTestContext) = context.test {
        val timer = TimerElement(2.Ticks)

        assertFalse(timer.complete, "Expected a fresh timer to not be complete")

        repeat(2) { timer.tick(server) }
        assertFalse(timer.complete, "Expected the timer to still be running")

        timer.tick(server)
        assertTrue(timer.complete, "Expected the timer to have completed")
    }

    @GameTest
    fun settingTheRemainingDurationKeepsTheTotal(context: ArcadeTestContext) = context.test {
        val timer = TimerElement(20.Ticks)
        timer.setRemainingDuration(5.Ticks)

        timer.getRemainingDuration() shouldEqual 5.Ticks
        timer.getTotalDuration() shouldEqual 20.Ticks
    }

    @GameTest
    fun aTimerWithoutADurationNeverAdvances(context: ArcadeTestContext) = context.test {
        val timer = TimerElement()

        assertFalse(timer.hasDuration, "Expected the timer to have no duration")

        repeat(5) { timer.tick(server) }

        timer.getProgress() shouldEqual 0.0F
        timer.getRemainingDuration() shouldEqual 0.Ticks
    }

    @GameTest
    fun removingTheDurationStopsTheCountdown(context: ArcadeTestContext) = context.test {
        val timer = TimerElement(10.Ticks)
        timer.tick(server)
        timer.removeDuration()

        assertFalse(timer.hasDuration, "Expected the duration to have been removed")
        assertTrue(timer.complete, "Expected removing the duration to complete the timer")

        repeat(5) { timer.tick(server) }
        timer.getRemainingDuration() shouldEqual 0.Ticks
    }

    @GameTest
    fun settingTheDurationRestartsTheTimer(context: ArcadeTestContext) = context.test {
        val timer = TimerElement(2.Ticks)
        repeat(3) { timer.tick(server) }
        assertTrue(timer.complete, "Expected the timer to have completed")

        timer.setTotalDuration(10.Ticks)

        assertFalse(timer.complete, "Expected setting the duration to restart the timer")
        timer.getRemainingDuration() shouldEqual 10.Ticks
    }

    @GameTest
    fun elementsReadTheTimerWithoutAdvancingIt(context: ArcadeTestContext) = context.test {
        val timer = TimerElement(10.Ticks)
        val progress = timer.progress()
        val remaining = timer.remaining { duration -> Component.literal("${duration.ticks}") }

        // Both elements are ticked, as they would be by a dynamic visual
        repeat(5) {
            progress.tick(server)
            remaining.tick(server)
        }

        timer.getRemainingDuration() shouldEqual 10.Ticks

        timer.tick(server)

        progress.get(server) shouldEqual 0.1F
        remaining.get(server) shouldEqual Component.literal("9")
    }
}
