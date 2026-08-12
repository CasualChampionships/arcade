/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.tests.server.npc

import net.casual.arcade.gametest.TestContext
import net.casual.arcade.gametest.utils.TestFakePlayer
import net.casual.arcade.gametest.utils.absolute
import net.casual.arcade.npc.ArcadeNPCs
import net.casual.arcade.npc.pathfinding.movement.types.ClimbMovementType
import net.casual.arcade.tests.server.ArcadeTestSuite
import net.casual.arcade.tests.server.npc.NPCCourse.assertArrives
import net.casual.arcade.tests.server.npc.NPCCourse.path
import net.casual.arcade.tests.server.npc.NPCCourse.route
import net.casual.arcade.tests.server.npc.NPCCourse.spawn
import net.casual.arcade.utils.TimeUtils.Seconds
import net.fabricmc.fabric.api.gametest.v1.GameTest
import net.minecraft.core.BlockPos
import net.minecraft.util.Mth

@Suppress("FunctionName", "Unused")
object NPCClimbingTests: ArcadeTestSuite() {
    override val namespace: String = ArcadeNPCs.MOD_ID

    private const val MAX_TURN = 30.0F

    @GameTest(structure = "arcade:ladder_up", maxTicks = 400)
    fun `climbs up ladder`(context: TestContext) = context.test {
        val player = spawn(context, 0, 1, 1)
        val goal = absolute(8, 4, 1)

        val path = path(context, player, goal, "up the ladder")
        context.assertTrue(
            path.movements.any { it.type == ClimbMovementType },
            "Reached a shelf four blocks up without climbing: ${route(path)}"
        )

        player.navigation.moveTo(path)
        assertArrives(context, player, goal)
    }

    @GameTest(structure = "arcade:ladder_down", maxTicks = 400)
    fun `climbs down ladder`(context: TestContext) = context.test {
        val player = spawn(context, 0, 5, 1)
        val goal = absolute(8, 1, 1)

        val path = path(context, player, goal, "down the ladder")
        context.assertTrue(
            path.movements.any { it.type == ClimbMovementType },
            "Left a shelf four blocks up without climbing: ${route(path)}"
        )

        player.navigation.moveTo(path)
        assertArrives(context, player, goal)
    }

    @GameTest(structure = "arcade:ladder_high_exit", maxTicks = 400)
    fun `climbs off top of ladder`(context: TestContext) = context.test {
        val player = spawn(context, 0, 1, 1)
        val goal = absolute(8, 4, 1)

        player.navigation.moveTo(path(context, player, goal, "over the top of the ladder"))
        assertArrives(context, player, goal)
    }

    @GameTest(structure = "arcade:ladder_face_on", maxTicks = 400)
    fun `climbs up ladder face on`(context: TestContext) = context.test {
        val player = spawn(context, 0, 1, 0)
        val goal = absolute(8, 4, 0)

        player.navigation.moveTo(path(context, player, goal, "over the top of the ladder"))
        assertArrives(context, player, goal)
    }

    @GameTest(structure = "arcade:scaffolding_tower", maxTicks = 400)
    fun `climbs scaffolding`(context: TestContext) = context.test {
        val player = spawn(context, 0, 1, 0)
        val goal = absolute(8, 4, 0)

        player.navigation.moveTo(path(context, player, goal, "up the scaffolding"))
        assertArrives(context, player, goal)
    }

    @GameTest(structure = "arcade:ladder_up", maxTicks = 400)
    fun `doesnt turn while climbing`(context: TestContext) = context.test {
        assertClimbsWithoutTurning(context, spawn(context, 0, 1, 1), absolute(8, 4, 1))
    }

    @GameTest(structure = "arcade:ladder_face_on", maxTicks = 400)
    fun `doesnt turn while climbing face on`(context: TestContext) = context.test {
        assertClimbsWithoutTurning(context, spawn(context, 0, 1, 0), absolute(8, 4, 0))
    }

    private suspend fun assertClimbsWithoutTurning(
        context: TestContext,
        player: TestFakePlayer,
        goal: BlockPos
    ) {
        player.navigation.moveTo(path(context, player, goal, "up the ladder"))

        context.assertEventually(5.Seconds, "Never took hold of the ladder") {
            player.onClimbable()
        }

        val facing = player.yRot
        context.assertNever(3.Seconds, "Turned while climbing") {
            player.onClimbable() && Mth.degreesDifferenceAbs(player.yRot, facing) > MAX_TURN
        }
    }

    @GameTest(structure = "arcade:ladder_up", maxTicks = 200)
    fun `doesnt climb when disabled`(context: TestContext) = context.test {
        val player = spawn(context, 0, 1, 1)
        val goal = absolute(8, 4, 1)
        player.navigation.settings.canClimb = false

        val path = player.navigation.createPath(goal, accuracy = 0)
        if (path != null) {
            context.assertFalse(path.reachesTarget, "Climbed a ladder with climbing turned off: ${route(path)}")
        }
    }
}
