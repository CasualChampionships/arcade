/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.tests.server.npc

import net.casual.arcade.gametest.TestContext
import net.casual.arcade.gametest.utils.TestFakePlayer
import net.casual.arcade.gametest.utils.absolute
import net.casual.arcade.npc.pathfinding.Path
import net.casual.arcade.utils.TimeUtils.Seconds
import net.casual.arcade.utils.entity.teleportTo
import net.casual.arcade.utils.math.location.Location
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.minecraft.core.BlockPos

/**
 * Shared plumbing for the pathfinding courses.
 *
 * Every course is a structure under `data/arcade-tests/gametest/structure`, laid out as a one
 * block wide corridor running along positive X at `z = 1`. The corridor is walled because the
 * game test framework fills everything below the structure with stone, so an NPC that can step
 * off the course will simply walk around it.
 *
 * Courses are ordinary structures, so they can be opened and edited in game with a structure
 * block rather than by hand.
 */
object NPCCourse {
    /**
     * Spawns an NPC standing at the course position [x], [y].
     *
     * @return The spawned NPC.
     */
    suspend fun spawn(context: TestContext, x: Int, y: Int, z: Int): TestFakePlayer {
        val player = context.createTestPlayer()
        val position = context.absolute(x, y, z)
        player.teleportTo(Location(position.x + 0.5, position.y.toDouble(), position.z + 0.5, 0.0F, 0.0F))
        return player
    }


    /**
     * Finds a path to [goal], failing the test if there is none.
     */
    fun path(context: TestContext, player: TestFakePlayer, goal: BlockPos, what: String): Path {
        val path = context.assertNotNull(
            player.navigation.createPath(goal, accuracy = 0),
            "No path $what from ${player.blockPosition()} to $goal"
        )
        context.assertTrue(path.reachesTarget, "Path $what did not reach $goal: ${route(path)}")
        return path
    }

    /**
     * Suspends until the NPC reaches [goal], failing the test if it never does.
     */
    suspend fun assertArrives(
        context: TestContext,
        player: TestFakePlayer,
        goal: BlockPos,
        timeout: MinecraftTimeDuration = 15.Seconds
    ) {
        context.assertEventually(timeout, "Never reached $goal, stopped at ${player.blockPosition()}") {
            player.blockPosition().distManhattan(goal) <= 1
        }
    }

    fun route(path: Path): String {
        return path.movements.joinToString(prefix = "[", postfix = "]") { it.type.id.path }
    }
}
