/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.pathfinding.evaluator

import net.casual.arcade.npc.FakePlayer
import net.casual.arcade.npc.pathfinding.NPCPathfindingContext
import net.casual.arcade.utils.registries.isOf
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.tags.BlockTags
import net.minecraft.util.Mth
import net.minecraft.world.level.PathNavigationRegion
import net.minecraft.world.level.pathfinder.Node
import net.minecraft.world.level.pathfinder.PathType
import net.minecraft.world.level.pathfinder.Target
import kotlin.math.max

public open class NPCAmphibiousNodeEvaluator: NPCWalkNodeEvaluator() {
    private var oldWaterCost: Float = 0.0F
    private var oldWalkableCost: Float = 0.0F
    private var oldWaterBorderCost: Float = 0.0F

    protected open fun getWalkableMalus(player: FakePlayer): Float {
        return 0.0F
    }

    protected open fun getWaterBorderMalus(player: FakePlayer): Float {
        return 8.0F
    }

    protected open fun getWaterMalus(player: FakePlayer): Float {
        return 2.0F
    }

    override fun prepare(level: PathNavigationRegion, player: FakePlayer) {
        super.prepare(level, player)

        this.oldWalkableCost = player.getPathfindingMalus(PathType.WALKABLE)
        player.setPathfindingMalus(PathType.WALKABLE, this.getWalkableMalus(player))
        this.oldWaterBorderCost = player.getPathfindingMalus(PathType.WATER_BORDER)
        player.setPathfindingMalus(PathType.WATER_BORDER, this.getWaterBorderMalus(player))
        this.oldWaterCost = player.getPathfindingMalus(PathType.WATER)
        player.setPathfindingMalus(PathType.WATER, this.getWaterMalus(player))
    }

    override fun done() {
        this.player?.setPathfindingMalus(PathType.WALKABLE, this.oldWalkableCost)
        this.player?.setPathfindingMalus(PathType.WATER_BORDER, this.oldWaterBorderCost)
        this.player?.setPathfindingMalus(PathType.WATER, this.oldWaterCost)
        super.done()
    }

    override fun getStart(): Node {
        val player = this.player ?: return super.getStart()
        if (!player.isInWater) {
            return super.getStart()
        }

        return this.getStartNode(BlockPos(
            Mth.floor(player.boundingBox.minX),
            Mth.floor(player.boundingBox.minY + 0.5),
            Mth.floor(player.boundingBox.minZ)
        ))
    }

    override fun getTarget(x: Double, y: Double, z: Double): Target {
        return this.getTargetNodeAt(x, y + 0.5, z)
    }

    override fun getNeighbors(outputArray: Array<Node?>, node: Node): Int {
        val player = this.player!!
        var neighborCount = super.getNeighbors(outputArray, node)
        val above = this.getCachedPathType(node.x, node.y + 1, node.z)
        val current = this.getCachedPathType(node.x, node.y, node.z)
        var verticalDeltaLimit = 0
        if (player.getPathfindingMalus(above) >= 0.0f && current != PathType.STICKY_HONEY) {
            verticalDeltaLimit = Mth.floor(max(1.0, player.maxUpStep().toDouble()).toFloat())
        }

        val floorLevel = this.getFloorLevel(BlockPos(node.x, node.y, node.z))
        val aboveAccepted = this.findAcceptedNode(
            node.x,
            node.y + 1,
            node.z,
            max(0.0, (verticalDeltaLimit - 1).toDouble()).toInt(),
            floorLevel,
            Direction.UP,
            current
        )
        val belowAccepted = this.findAcceptedNode(
            node.x, node.y - 1, node.z, verticalDeltaLimit, floorLevel, Direction.DOWN, current
        )
        if (this.isVerticalNeighborValid(aboveAccepted, node)) {
            outputArray[neighborCount++] = aboveAccepted
        }

        if (this.isVerticalNeighborValid(belowAccepted, node) && current != PathType.TRAPDOOR) {
            outputArray[neighborCount++] = belowAccepted
        }

        return neighborCount
    }

    private fun isVerticalNeighborValid(neighbor: Node?, node: Node): Boolean {
        return neighbor != null && this.isNeighborValid(neighbor, node) && neighbor.type == PathType.WATER
    }

    override fun isAmphibious(): Boolean {
        return true
    }

     override fun getPathType(context: NPCPathfindingContext, x: Int, y: Int, z: Int): PathType {
         val type = context.getPathTypeFromState(x, y, z)
         if (type == PathType.WATER) {
             val mutableBlockPos = BlockPos.MutableBlockPos()

             for (direction in Direction.entries) {
                 mutableBlockPos.set(x, y, z).move(direction)
                 val pathType2 = context.getPathTypeFromState(mutableBlockPos.x, mutableBlockPos.y, mutableBlockPos.z)
                 if (pathType2 == PathType.BLOCKED) {
                     return PathType.WATER_BORDER
                 }
             }

             return PathType.WATER
         }


         // This is a gross hack to make ladders/vines pathfind-able
         val pos = BlockPos(x, y, z)
         val state = context.getBlockState(pos)
         if (state.isOf(BlockTags.CLIMBABLE)) {
             // Maybe we should check the collisions of the scaffolding
             return PathType.WATER
         }

         return super.getPathType(context, x, y, z)
     }
}