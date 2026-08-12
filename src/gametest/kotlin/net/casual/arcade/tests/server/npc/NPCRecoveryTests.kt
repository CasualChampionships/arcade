/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.tests.server.npc

import net.casual.arcade.gametest.TestContext
import net.casual.arcade.gametest.utils.TestFakePlayer
import net.casual.arcade.gametest.utils.absolute
import net.casual.arcade.gametest.utils.createTestPlayer
import net.casual.arcade.gametest.utils.fill
import net.casual.arcade.npc.ArcadeNPCs
import net.casual.arcade.tests.server.ArcadeTestSuite
import net.casual.arcade.utils.TimeUtils.Seconds
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.coroutine.delay
import net.casual.arcade.utils.entity.teleportTo
import net.casual.arcade.utils.math.location.with
import net.fabricmc.fabric.api.gametest.v1.GameTest
import net.minecraft.world.level.block.Blocks

@Suppress("FunctionName", "Unused")
object NPCRecoveryTests: ArcadeTestSuite() {
    override val namespace: String = ArcadeNPCs.MOD_ID

    @GameTest(structure = "arcade:room", maxTicks = 400)
    fun `repaths when the way ahead is blocked`(context: TestContext) = context.test {
        val player = createTestPlayer(0, 1, 2)
        val goal = absolute(10, 1, 2)
        player.navigation.moveTo(path(player, goal, "across the room"))

        delay(10.Ticks)
        val original = player.navigation.path
        context.fill(5, 1, 0, 5, 3, 3, Blocks.OAK_PLANKS)

        context.assertEventually(1.Seconds, "Walling off the lane did not produce a new path") {
            player.navigation.path != original
        }

        assertArrives(player, goal)
        context.assertFalse(player.navigation.isStuck, "Gave up despite a way round being open")
    }

    @GameTest(structure = "arcade:room", maxTicks = 400)
    fun `keeps going after being knocked off its path`(context: TestContext) = context.test {
        val player = createTestPlayer(0, 1, 2)
        val goal = absolute(10, 1, 2)
        player.navigation.moveTo(path(player, goal, "across the room"))

        delay(10.Ticks)
        player.teleportTo(context.absolute(2.5, 1.0, 4.5).with(player.rotationVector))

        assertArrives(player, goal)
        context.assertFalse(player.navigation.isStuck, "Gave up after being moved off its path")
    }

    @GameTest(structure = "arcade:room", maxTicks = 400)
    fun `gives up once the room is walled off`(context: TestContext) = context.test {
        val player = createTestPlayer(0, 1, 2)
        val goal = absolute(10, 1, 2)
        player.navigation.moveTo(path(player, goal, "across the room"))

        delay(10.Ticks)
        context.fill(5, 1, 0, 5, 3, 4, Blocks.OAK_PLANKS)

        context.assertEventually(10.Seconds, "Never gave up on a target it cannot reach") {
            player.navigation.isStuck
        }
        context.assertTrue(player.navigation.isDone(), "Still following a path after giving up")
    }

    @GameTest(structure = "arcade:twin_ladders", maxTicks = 600)
    fun `finds the other ladder when one is broken under it`(context: TestContext) = context.test {
        val player = createTestPlayer(0, 1, 2)
        val goal = absolute(10, 4, 2)
        player.navigation.moveTo(path(player, goal, "up a ladder"))

        context.assertEventually(10.Seconds, "Never started climbing either ladder") {
            player.onClimbable() && player.blockPosition().y >= context.absolute(0, 2, 0).y
        }

        val lane = player.blockPosition().z - context.absolute(0, 0, 0).z
        context.fill(4, 1, lane, 4, 3, lane, Blocks.AIR)

        val firstLane = 1
        val secondLane = 3
        val other = if (lane == firstLane) secondLane else firstLane
        context.assertEventually(10.Seconds, "Never moved over to the other ladder") {
            player.onClimbable() && player.blockPosition().z - context.absolute(0, 0, 0).z == other
        }

        assertArrives(player, goal)
    }
}
