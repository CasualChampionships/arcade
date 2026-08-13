/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.tests.server.npc

import net.casual.arcade.gametest.TestContext
import net.casual.arcade.gametest.utils.absolute
import net.casual.arcade.npc.ArcadeNPCs
import net.casual.arcade.npc.pathfinding.movement.types.AscendMovementType
import net.casual.arcade.npc.pathfinding.movement.types.DiagonalMovementType
import net.casual.arcade.npc.pathfinding.movement.types.FallMovementType
import net.casual.arcade.tests.server.ArcadeTestSuite
import net.casual.arcade.utils.TimeUtils.Seconds
import net.fabricmc.fabric.api.gametest.v1.GameTest
import net.minecraft.util.Mth
import net.minecraft.world.level.block.Blocks

@Suppress("FunctionName", "Unused")
object NPCPathfindingTests: ArcadeTestSuite() {
    override val namespace: String = ArcadeNPCs.MOD_ID

    private const val EAST_YAW = -90.0F
    private const val FACING_TOLERANCE = 5.0F

    @GameTest(structure = "arcade:walkway", maxTicks = 400)
    fun `faces the direction it is walking`(context: TestContext) = context.test {
        val player = player(0, 1, 0).spawn()
        val goal = absolute(9, 1, 0)
        player.navigation.moveTo(path(player, goal, "along the walkway"))

        context.assertEventually(5.Seconds, "Never turned to face the way it was walking") {
            Mth.degreesDifferenceAbs(player.yRot, EAST_YAW) < FACING_TOLERANCE
        }
        context.assertNever(1.Seconds, "Head stopped following the body") {
            Mth.degreesDifferenceAbs(player.yRot, player.yHeadRot) >= FACING_TOLERANCE
        }
    }

    @GameTest(structure = "arcade:slab_staircase", maxTicks = 400)
    fun `walks up slab staircase without jumping`(context: TestContext) = context.test {
        val player = player(0, 1, 0).spawn()
        val goal = absolute(9, 3, 0)

        val path = path(player, goal, "up the slab staircase")
        context.assertTrue(
            path.movements.none { it.type == AscendMovementType },
            "Jumped up a slab staircase: ${route(path)}"
        )

        player.navigation.moveTo(path)
        assertArrives(player, goal)
    }

    @GameTest(structure = "arcade:trapdoor", maxTicks = 400)
    fun `walks over closed trapdoor without jumping`(context: TestContext) = context.test {
        val player = player(0, 1, 0).spawn()
        val goal = absolute(9, 1, 0)

        val path = path(player, goal, "over the trapdoor")
        context.assertTrue(
            path.movements.none { it.type == AscendMovementType },
            "Jumped over a closed trapdoor: ${route(path)}"
        )

        player.navigation.moveTo(path)
        assertArrives(player, goal)
    }

    @GameTest(structure = "arcade:stairs_low_side", maxTicks = 400)
    fun `walks up stairs without jumping`(context: TestContext) = context.test {
        val player = player(0, 1, 0).spawn()
        val goal = absolute(9, 3, 0)

        val path = path(player, goal, "up the stairs")
        context.assertTrue(
            path.movements.none { it.type == AscendMovementType },
            "Jumped up a stair approached from its low side: ${route(path)}"
        )

        player.navigation.moveTo(path)
        assertArrives(player, goal)
    }

    @GameTest(structure = "arcade:stairs_high_side", maxTicks = 400)
    fun `jumps up stair approached from its high side`(context: TestContext) = context.test {
        val player = player(0, 1, 0).spawn()
        val goal = absolute(9, 1, 0)

        val path = path(player, goal, "up the stairs")
        context.assertTrue(
            path.movements.any { it.type == AscendMovementType },
            "Walked up a stair's full block face without jumping: ${route(path)}"
        )

        player.navigation.moveTo(path)
        assertArrives(player, goal)
    }

    @GameTest(structure = "arcade:step_up", maxTicks = 400)
    fun `jumps up full block`(context: TestContext) = context.test {
        val player = player(0, 1, 0).spawn()
        val goal = absolute(9, 1, 0)

        val path = path(player, goal, "up the step")
        context.assertTrue(
            path.movements.any { it.type == AscendMovementType },
            "Expected to jump up a full block, planned ${route(path)}"
        )

        player.navigation.moveTo(path)
        assertArrives(player, goal)
    }

    @GameTest(structure = "arcade:ledge", maxTicks = 400)
    fun `drops down ledge`(context: TestContext) = context.test {
        val player = player(0, 3, 0).spawn()
        val goal = absolute(9, 1, 0)

        val path = path(player, goal, "down the ledge")
        context.assertTrue(
            path.movements.any { it.type == FallMovementType },
            "Expected to drop off the ledge, planned ${route(path)}"
        )

        player.navigation.moveTo(path)
        assertArrives(player, goal)
    }

    @GameTest(structure = "arcade:ledge", maxTicks = 400)
    fun `doesnt turn while falling off ledge`(context: TestContext) = context.test {
        val player = player(0, 3, 0).spawn()
        val goal = absolute(9, 1, 0)
        context.assertEventually(5.Seconds, "Never landed on the ledge") { player.onGround() }

        player.navigation.moveTo(path(player, goal, "down the ledge"))
        context.assertEventually(5.Seconds, "Never left the ledge") { !player.onGround() }

        context.assertNever(2.Seconds, "Turned away from the drop while falling") {
            !player.onGround() && Mth.degreesDifferenceAbs(player.yRot, EAST_YAW) > FACING_TOLERANCE
        }
    }

    @GameTest(structure = "arcade:sealed", maxTicks = 200)
    fun `reports unreachable target`(context: TestContext) = context.test {
        val player = player(0, 1, 0).spawn()
        val goal = absolute(8, 1, 0)

        val path = player.navigation.createPath(goal, accuracy = 0)
        if (path != null) {
            context.assertFalse(
                path.reachesTarget,
                "Claimed to reach a sealed-off target: ${route(path)}"
            )
        }
    }

    @GameTest(structure = "arcade:zigzag", maxTicks = 200)
    fun `pathfinds around walls`(context: TestContext) = context.test {
        val player = player(0, 1, 0).spawn()
        val goal = absolute(4, 1, 4)

        player.navigation.moveTo(path(player, goal, "along the zigzag"))
        assertArrives(player, goal)
    }

    @GameTest(structure = "arcade:ascend", maxTicks = 200)
    fun `pathfinds upwards`(context: TestContext) = context.test {
        val player = player(0, 1, 0).spawn()
        val goal = absolute(4, 9, 4)

        player.navigation.moveTo(path(player, goal, "up the structure"))
        assertArrives(player, goal)
    }

    @GameTest(structure = "arcade:soulsand", maxTicks = 200)
    fun `pathfinding avoids soulsand`(context: TestContext) = context.test {
        val player = player(0, 2, 1).spawn()
        val goal = absolute(6, 2, 2)

        val path = path(player, goal, "avoiding the soulsand")
        assertFalse(path.hasSupportingBlock(context, Blocks.SOUL_SAND), "Preferred soulsand")

        player.navigation.moveTo(path)
        assertArrives(player, goal)
    }

    @GameTest(structure = "arcade:platform", maxTicks = 200)
    fun `pathfinding walks diagonally`(context: TestContext) = context.test {
        val player = player(0, 1, 0).spawn()
        val goal = absolute(4, 1, 4)

        val path = path(player, goal, "along platform")
        assertTrue(path.movements.all { it.type == DiagonalMovementType }, "Didn't walk diagonally")

        player.navigation.moveTo(path)
        assertArrives(player, goal)
    }
}
