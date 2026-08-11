/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.tests.server.scheduler

import net.casual.arcade.gametest.TestContext
import net.casual.arcade.scheduler.ArcadeScheduler
import net.casual.arcade.tests.server.ArcadeTestSuite
import net.casual.arcade.scheduler.GlobalTickedScheduler
import net.casual.arcade.utils.TimeUtils.Seconds
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.virtual.visuals.ArcadeVirtualVisuals
import net.fabricmc.fabric.api.gametest.v1.GameTest

@Suppress("FunctionName", "Unused")
object GlobalTickedSchedulerTests: ArcadeTestSuite() {
    override val namespace: String = ArcadeScheduler.MOD_ID

    @GameTest(maxTicks = 200)
    fun `task runs later on global scheduler`(context: TestContext) = context.test {
        var ran = false
        GlobalTickedScheduler.Server.later { ran = true }

        assertEventually(5.Seconds, "Global scheduler never ran a task scheduled with later") { ran }
    }

    @GameTest(maxTicks = 200)
    fun `task runs after delay on global scheduler`(context: TestContext) = context.test {
        var ran = false
        GlobalTickedScheduler.Server.schedule(10.Ticks) { ran = true }

        assertEventually(5.Seconds, "Global scheduler never ran a delayed task") { ran }
    }

    @GameTest(maxTicks = 200)
    fun `task runs on temporary scheduler`(context: TestContext) = context.test {
        val temporary = GlobalTickedScheduler.Server.temporaryScheduler(5.Seconds)
        var ran = false
        temporary.schedule(5.Ticks) { ran = true }

        assertEventually(5.Seconds, "Temporary scheduler was never ticked") { ran }
    }

    @GameTest(maxTicks = 200)
    fun `task expires on temporary scheduler`(context: TestContext) = context.test {
        val temporary = GlobalTickedScheduler.Server.temporaryScheduler(10.Ticks)
        var ran = false
        val handle = temporary.schedule(5.Seconds) { ran = true }

        assertEventually(5.Seconds, "Temporary scheduler outlived its lifetime") { handle.isFinished }
        assertFalse(ran, "Task on an expired temporary scheduler still ran")
    }
}
