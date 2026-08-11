/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.tests.server.scheduler

import net.casual.arcade.gametest.TestContext
import net.casual.arcade.scheduler.ArcadeScheduler
import net.casual.arcade.tests.server.ArcadeTestSuite
import net.casual.arcade.scheduler.SimpleTickedScheduler
import net.casual.arcade.scheduler.task.impl.LevelTask
import net.casual.arcade.scheduler.task.impl.PlayerTask
import net.casual.arcade.utils.player.username
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.fabricmc.fabric.api.gametest.v1.GameTest
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer

@Suppress("FunctionName", "Unused")
object TaskTests: ArcadeTestSuite() {
    override val namespace: String = ArcadeScheduler.MOD_ID

    @GameTest
    fun `level task resolves level`(context: TestContext) = context.test {
        val scheduler = SimpleTickedScheduler.server()
        var resolved: ServerLevel? = null
        scheduler.schedule(MinecraftTimeDuration.ZERO, LevelTask(level) { resolved = it })

        scheduler.tick()
        assertEquals(level.dimension(), assertNotNull(resolved).dimension())
    }

    @GameTest(maxTicks = 400)
    fun `player task resolves player`(context: TestContext) = context.test {
        val player = createTestPlayer()
        val scheduler = SimpleTickedScheduler.server()
        var resolved: ServerPlayer? = null
        scheduler.schedule(MinecraftTimeDuration.ZERO, PlayerTask(player) { resolved = it })

        scheduler.tick()
        assertEquals(player.username, assertNotNull(resolved).username)
    }

    @GameTest(maxTicks = 400)
    fun `player task skipped when player leaves`(context: TestContext) = context.test {
        val player = createTestPlayer()
        val scheduler = SimpleTickedScheduler.server()
        var ran = false
        scheduler.schedule(MinecraftTimeDuration.ZERO, PlayerTask(player) { ran = true })

        server.playerList.remove(player)
        scheduler.tick()
        assertFalse(ran, "PlayerTask ran for a player who had left the server")
    }
}
