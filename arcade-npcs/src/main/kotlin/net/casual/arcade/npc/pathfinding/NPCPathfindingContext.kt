package net.casual.arcade.npc.pathfinding

import io.netty.util.internal.shaded.org.jctools.util.UnsafeAccess
import net.casual.arcade.npc.FakePlayer
import net.casual.arcade.npc.mixins.pathfinding.PathfindingContextAccessor
import net.minecraft.core.BlockPos
import net.minecraft.world.level.CollisionGetter
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.pathfinder.PathType
import net.minecraft.world.level.pathfinder.PathfindingContext

public class NPCPathfindingContext(
    public val level: CollisionGetter,
    public val player: FakePlayer
) {
    private val context by lazy { this.createPathfindingContext() }

    private val cache = this.player.serverLevel().pathTypeCache
    private val pos = BlockPos.MutableBlockPos()

    public val position: BlockPos = this.player.blockPosition()

    public fun getPathTypeFromState(x: Int, y: Int, z: Int): PathType {
        val blockPos = this.pos.set(x, y, z)
        return this.cache.getOrCompute(this.level, blockPos)
    }

    public fun getBlockState(pos: BlockPos): BlockState {
        return this.level.getBlockState(pos)
    }

    public fun asPathfindingContext(): PathfindingContext {
        return this.context
    }

    private fun createPathfindingContext(): PathfindingContext {
        val context = UnsafeAccess.UNSAFE.allocateInstance(PathfindingContext::class.java) as PathfindingContext
        context as PathfindingContextAccessor
        context.setCache(this.cache)
        context.setLevel(this.level)
        context.setMutablePos(this.pos)
        context.setMobPosition(this.position)
        return context
    }
}
