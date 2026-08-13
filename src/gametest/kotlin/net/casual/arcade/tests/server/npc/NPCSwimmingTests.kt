/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.tests.server.npc

import net.casual.arcade.gametest.TestContext
import net.casual.arcade.gametest.utils.absolute
import net.casual.arcade.npc.ArcadeNPCs
import net.casual.arcade.npc.pathfinding.movement.types.ClimbMovementType
import net.casual.arcade.npc.pathfinding.movement.types.SwimMovementType
import net.casual.arcade.tests.server.ArcadeTestSuite
import net.casual.arcade.utils.TimeUtils.Seconds
import net.fabricmc.fabric.api.gametest.v1.GameTest

@Suppress("FunctionName", "Unused")
object NPCSwimmingTests: ArcadeTestSuite() {
    override val namespace: String = ArcadeNPCs.MOD_ID

    @GameTest(structure = "arcade:pool", maxTicks = 600)
    fun `swims across pool`(context: TestContext) = context.test {
        val player = player(0, 4, 1).spawn()
        val goal = absolute(10, 4, 1)
        player.navigation.settings.canSwim = true

        val path = path(player, goal, "across the pool")
        context.assertTrue(
            path.movements.any { it.type == SwimMovementType },
            "Crossed a pool without swimming: ${route(path)}"
        )

        player.navigation.moveTo(path)
        assertArrives(player, goal)
    }

    @GameTest(structure = "arcade:pool_ladder", maxTicks = 600)
    fun `climbs out of pool up ladder`(context: TestContext) = context.test {
        val player = player(0, 4, 1).spawn()
        val goal = absolute(10, 6, 1)
        player.navigation.settings.canSwim = true

        val path = path(player, goal, "out of the pool")
        context.assertTrue(
            path.movements.any { it.type == SwimMovementType } &&
                path.movements.any { it.type == ClimbMovementType },
            "Expected to swim then climb, planned ${route(path)}"
        )

        player.navigation.moveTo(path)
        assertArrives(player, goal)
    }

    @GameTest(structure = "arcade:pool_long", maxTicks = 800)
    fun `sinks to sprint swim`(context: TestContext) = context.test {
        val player = player(0, 4, 1).spawn()
        val goal = absolute(23, 4, 1)
        player.navigation.settings.canSwim = true

        val path = path(player, goal, "along the pool")
        val surface = context.absolute(0, 3, 0).y.toDouble()
        val deepest = path.movements.minOf { it.to.surface }
        context.assertTrue(
            deepest < surface,
            "Wallowed along the surface instead of sinking to sprint: ${route(path)}"
        )

        player.navigation.moveTo(path)
        context.assertEventually(15.Seconds, "Never sprinted while swimming") {
            player.isUnderWater && player.isSprinting
        }

        assertArrives(player, goal)
    }

    @GameTest(structure = "arcade:pool_long", maxTicks = 400)
    fun `doesnt sprint swim when sprinting is disabled`(context: TestContext) = context.test {
        val player = player(0, 4, 1).spawn()
        val goal = absolute(23, 4, 1)
        player.navigation.settings.canSwim = true
        player.navigation.settings.canSprint = false
        player.navigation.moveTo(path(player, goal, "along the pool"))

        context.assertNever(10.Seconds, "Sprinted with sprinting disabled") {
            player.isSprinting
        }
    }

    @GameTest(structure = "arcade:pool", maxTicks = 200)
    fun `doesnt swim when swimming is disabled`(context: TestContext) = context.test {
        val player = player(0, 4, 1).spawn()

        val path = player.navigation.createPath(absolute(10, 4, 1), accuracy = 0)
        if (path != null) {
            context.assertFalse(
                path.reachesTarget,
                "Crossed a pool with swimming turned off: ${route(path)}"
            )
        }
    }
}
