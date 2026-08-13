package net.casual.arcade.tests.server.npc

import net.casual.arcade.gametest.TestContext
import net.casual.arcade.npc.pathfinding.Path
import net.casual.arcade.npc.pathfinding.PathNode
import net.casual.arcade.utils.registries.isOf
import net.minecraft.world.level.block.Block

fun Path.hasSupportingBlock(context: TestContext, block: Block): Boolean {
    var node: PathNode? = this.destination
    while (node != null) {
        if (context.level.getBlockState(node.support).isOf(block)) {
            return true
        }
        node = node.previous
    }
    return false
}