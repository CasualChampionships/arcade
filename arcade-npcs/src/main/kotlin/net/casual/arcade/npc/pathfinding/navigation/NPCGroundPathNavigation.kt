/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.pathfinding.navigation

import net.casual.arcade.npc.FakePlayer
import net.casual.arcade.npc.pathfinding.NPCPathfinder
import net.casual.arcade.npc.pathfinding.evaluator.NPCWalkNodeEvaluator
import net.casual.arcade.utils.isOf
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.SectionPos
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.pathfinder.Node
import net.minecraft.world.level.pathfinder.Path
import net.minecraft.world.level.pathfinder.PathType
import net.minecraft.world.phys.Vec3

public open class NPCGroundPathNavigation(player: FakePlayer): NPCPathNavigation(player) {
    private var avoidSun: Boolean = false

    override fun createPathfinder(maxVisitedNodes: Int): NPCPathfinder {
        this.nodeEvaluator = NPCWalkNodeEvaluator()
        return NPCPathfinder(this.nodeEvaluator, maxVisitedNodes)
    }

    override fun canUpdatePath(): Boolean {
        return this.player.onGround() || this.player.isInLiquid || this.player.isPassenger
    }

    override fun getTempMobPos(): Vec3 {
        return Vec3(this.player.x, getSurfaceY().toDouble(), this.player.z)
    }

    override fun createPath(pos: BlockPos, accuracy: Int): Path? {
        var newPos = pos
        val levelChunk = this.level.chunkSource.getChunkNow(
            SectionPos.blockToSectionCoord(newPos.x),
            SectionPos.blockToSectionCoord(newPos.z)
        ) ?: return null
        if (levelChunk.getBlockState(newPos).isAir) {
            val mutablePos = newPos.mutable().move(Direction.DOWN)
            while (mutablePos.y > this.level.minY && levelChunk.getBlockState(mutablePos).isAir) {
                mutablePos.move(Direction.DOWN)
            }
            if (mutablePos.y > this.level.minY) {
                return super.createPath(mutablePos.above(), accuracy)
            }
            mutablePos.setY(newPos.y + 1)
            while (mutablePos.y <= this.level.maxY && levelChunk.getBlockState(mutablePos).isAir) {
                mutablePos.move(Direction.UP)
            }
            newPos = mutablePos
        }
        if (!levelChunk.getBlockState(newPos).isSolid) {
            return super.createPath(newPos, accuracy)
        }
        val mutablePos = newPos.mutable().move(Direction.UP)
        while (mutablePos.y <= this.level.maxY && levelChunk.getBlockState(mutablePos).isSolid) {
            mutablePos.move(Direction.UP)
        }
        return super.createPath(mutablePos.immutable(), accuracy)
    }

    override fun createPath(entity: Entity, accuracy: Int): Path? {
        return this.createPath(entity.blockPosition(), accuracy)
    }

    private fun getSurfaceY(): Int {
        return if (this.player.isInWater && this.canFloat()) {
            var i = this.player.blockY
            var state = this.level.getBlockState(BlockPos.containing(this.player.x, i.toDouble(), this.player.z))
            var j = 0
            while (state.isOf(Blocks.WATER)) {
                state = this.level.getBlockState(BlockPos.containing(this.player.x, (++i).toDouble(), this.player.z))
                if (++j > 16) {
                    return this.player.blockY
                }
            }
            i
        } else {
            Mth.floor(this.player.y + 0.5)
        }
    }

    override fun trimPath() {
        super.trimPath()
        if (this.avoidSun) {
            if (this.level.canSeeSky(BlockPos.containing(this.player.x, this.player.y + 0.5, this.player.z))) {
                return
            }
            for (i in 0 until this.path!!.nodeCount) {
                val node: Node = this.path!!.getNode(i)
                if (this.level.canSeeSky(BlockPos(node.x, node.y, node.z))) {
                    this.path!!.truncateNodes(i)
                    return
                }
            }
        }
    }

    protected fun hasValidPathType(pathType: PathType): Boolean {
        return when (pathType) {
            PathType.WATER -> false
            PathType.LAVA -> false
            else -> pathType != PathType.OPEN
        }
    }

    public fun setCanOpenDoors(canOpenDoors: Boolean) {
        this.nodeEvaluator.canOpenDoors = canOpenDoors
    }

    public fun setAvoidSun(avoidSun: Boolean) {
        this.avoidSun = avoidSun
    }

    public fun setCanWalkOverFences(canWalkOverFences: Boolean) {
        this.nodeEvaluator.canWalkOverFences = canWalkOverFences
    }
}
