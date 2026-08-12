package net.casual.arcade.tests.server.npc

import net.casual.arcade.gametest.TestContext
import net.casual.arcade.gametest.utils.TestFakePlayer
import net.casual.arcade.npc.pathfinding.Path
import net.casual.arcade.utils.TimeUtils.Seconds
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.minecraft.core.BlockPos

fun route(path: Path): String {
    return path.movements.joinToString(prefix = "[", postfix = "]") { it.type.id.path }
}

fun TestContext.path(player: TestFakePlayer, goal: BlockPos, what: String): Path {
    val path = assertNotNull(
        player.navigation.createPath(goal, accuracy = 0),
        "No path $what from ${player.blockPosition()} to $goal"
    )
    assertTrue(path.reachesTarget, "Path $what did not reach $goal: ${route(path)}")
    return path
}

suspend fun TestContext.assertArrives(player: TestFakePlayer, goal: BlockPos, timeout: MinecraftTimeDuration = 15.Seconds) {
    assertEventually(timeout, "Never reached $goal, stopped at ${player.blockPosition()}") {
        player.blockPosition().distManhattan(goal) <= 1
    }
}