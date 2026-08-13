/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.tests.server.npc

import net.casual.arcade.gametest.TestContext
import net.casual.arcade.gametest.utils.TestFakePlayer
import net.casual.arcade.gametest.utils.absolute
import net.casual.arcade.npc.ArcadeNPCs
import net.casual.arcade.npc.pathfinding.movement.types.ParkourMovementType
import net.casual.arcade.tests.server.ArcadeTestSuite
import net.casual.arcade.utils.TimeUtils.Seconds
import net.fabricmc.fabric.api.gametest.v1.GameTest
import net.minecraft.core.BlockPos

@Suppress("FunctionName", "Unused")
object NPCParkourTests: ArcadeTestSuite() {
    override val namespace: String = ArcadeNPCs.MOD_ID

    @GameTest(structure = "arcade:walkway", maxTicks = 400)
    fun `sprints along straight path`(context: TestContext) = context.test {
        val player = player(0, 1, 0).spawn()
        val goal = absolute(9, 1, 0)
        player.navigation.moveTo(goal, accuracy = 0)

        context.assertEventually(5.Seconds, "Never started sprinting down a straight corridor") {
            player.isSprinting
        }
        assertArrives(player, goal)
    }

    @GameTest(structure = "arcade:walkway", maxTicks = 300)
    fun `doesnt sprint when sprint disabled`(context: TestContext) = context.test {
        val player = player(0, 1, 0).spawn()
        val goal = absolute(9, 1, 0)
        player.navigation.settings.canSprint = false
        player.navigation.moveTo(goal, accuracy = 0)

        context.assertNever(3.Seconds, "Sprinted with sprinting disabled") {
            player.isSprinting
        }
    }

    @GameTest(structure = "arcade:parkour_gap_2", maxTicks = 400)
    fun `jumps 2 block gap`(context: TestContext) = context.test {
        val player = player(0, 2, 0).spawn()
        val goal = absolute(14, 2, 0)
        player.navigation.settings.canSprint = false

        assertCrossesGap(context, player, goal)
    }

    @GameTest(structure = "arcade:parkour_gap_3", maxTicks = 400)
    fun `jumps 3 block gap`(context: TestContext) = context.test {
        val player = player(0, 2, 0).spawn()
        val goal = absolute(14, 2, 0)

        assertCrossesGap(context, player, goal)
    }

    @GameTest(structure = "arcade:parkour_gap_3", maxTicks = 200)
    fun `doesnt jump 3 block gap when walking`(context: TestContext) = context.test {
        val player = player(0, 2, 0).spawn()
        val goal = absolute(14, 2, 0)
        player.navigation.settings.canSprint = false

        assertCannotCrossGap(context, player, goal, "the longest jump without sprinting")
    }

    @GameTest(structure = "arcade:parkour_gap_4", maxTicks = 200)
    fun `doesnt jump 4 block gap`(context: TestContext) = context.test {
        val player = player(0, 2, 0).spawn()
        val goal = absolute(14, 2, 0)

        assertCannotCrossGap(context, player, goal, "a 4 block jump")
    }

    @GameTest(structure = "arcade:parkour_gap_up", maxTicks = 400)
    fun `jumps a gap onto a ledge`(context: TestContext) = context.test {
        val player = player(0, 2, 0).spawn()
        val goal = absolute(14, 3, 0)

        assertCrossesGap(context, player, goal)
    }

    @GameTest(structure = "arcade:parkour_gap_up", maxTicks = 200)
    fun `doesnt jump onto a ledge when walking`(context: TestContext) = context.test {
        val player = player(0, 2, 0).spawn()
        val goal = absolute(14, 3, 0)
        player.navigation.settings.canSprint = false

        assertCannotCrossGap(context, player, goal, "a jump uphill without sprinting")
    }

    @GameTest(structure = "arcade:parkour_gap_down", maxTicks = 400)
    fun `jumps a gap off a ledge`(context: TestContext) = context.test {
        val player = player(0, 3, 0).spawn()
        val goal = absolute(14, 2, 0)

        assertCrossesGap(context, player, goal)
    }

    @GameTest(structure = "arcade:parkour_gap_2", maxTicks = 200)
    fun `doesnt jump when parkour disabled`(context: TestContext) = context.test {
        val player = player(0, 2, 0).spawn()
        val goal = absolute(14, 2, 0)
        player.navigation.settings.canParkour = false

        assertCannotCrossGap(context, player, goal, "a jump with parkour disabled")
    }

    private suspend fun assertCrossesGap(context: TestContext, player: TestFakePlayer, goal: BlockPos) {
        val path = context.assertNotNull(
            player.navigation.createPath(goal, accuracy = 0),
            "No path across the gap"
        )
        context.assertTrue(path.reachesTarget, "Path did not cross the gap")
        context.assertTrue(
            path.movements.any { it.type == ParkourMovementType },
            "Expected to jump across the gap, planned ${route(path)}"
        )

        player.navigation.moveTo(path)
        context.assertArrives(player, goal)
    }

    private fun assertCannotCrossGap(context: TestContext, player: TestFakePlayer, goal: BlockPos, what: String) {
        val path = player.navigation.createPath(goal, accuracy = 0)
        if (path != null) {
            context.assertFalse(path.reachesTarget, "Planned $what: ${route(path)}")
        }
    }
}
