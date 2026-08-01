/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.tests.server.gametest

import net.casual.arcade.gametest.ArcadeTestContext
import net.casual.arcade.gametest.ArcadeTestSuite
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.coroutine.delay
import net.fabricmc.fabric.api.gametest.v1.GameTest

object CoroutineTests: ArcadeTestSuite() {
    @GameTest(maxTicks = 100)
    fun delayResumes(context: ArcadeTestContext) = context.test {
        delay(1.Ticks)
    }

    @GameTest(maxTicks = 100)
    fun delayResumesLater(context: ArcadeTestContext) = context.test {
        val before = server.tickCount
        delay(5.Ticks)
        assertTrue(server.tickCount > before, "Server tickCount was same before and after delay!")
    }
}
