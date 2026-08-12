/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.pathfinding

import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.casual.arcade.npc.FakePlayer
import net.casual.arcade.npc.pathfinding.movement.MovementType
import net.casual.arcade.utils.registries.isOf
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.tags.BlockTags
import net.minecraft.util.Mth
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.CollisionGetter
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.LadderBlock
import net.minecraft.world.level.block.TrapDoorBlock
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.shapes.Shapes

public class PathfindingContext(
    public val level: CollisionGetter,
    public val player: FakePlayer,
    public val settings: PathfindingSettings
) {
    private val blocks = Long2ObjectOpenHashMap<BlockPathfindingInfo>(INITIAL_BLOCKS)
    private val heights = Long2DoubleOpenHashMap(INITIAL_BLOCKS).also {
        it.defaultReturnValue(Double.NaN)
    }
    private val clearances = Long2ByteOpenHashMap()
    private val mutable = BlockPos.MutableBlockPos()
    private val types = this.player.level().pathTypeCache

    public val width: Double = this.player.bbWidth.toDouble()
    public val height: Double = this.player.bbHeight.toDouble()
    public val stepHeight: Double = this.player.maxUpStep().toDouble()

    public val jumpSimulation: JumpPhysicsSimulation = JumpPhysicsSimulation(this.player)

    public val minY: Int = this.level.minY
    public val maxY: Int = this.level.minY + this.level.height - 1

    public fun getBlock(x: Int, y: Int, z: Int): BlockPathfindingInfo {
        val key = BlockPos.asLong(x, y, z)
        val cached = this.blocks.get(key)
        if (cached != null) {
            return cached
        }

        val pos = this.mutable.set(x, y, z)
        val state = this.level.getBlockState(pos)
        val climbable = state.isOf(BlockTags.CLIMBABLE)
        val info = BlockPathfindingInfo(
            state,
            state.fluidState,
            if (climbable) Shapes.empty() else state.getCollisionShape(this.level, pos),
            this.types.getOrCompute(this.level, pos),
            !climbable && state.isCollisionShapeFullBlock(this.level, pos)
        )
        this.blocks.put(key, info)
        return info
    }

    public fun getStandHeight(x: Int, y: Int, z: Int): Double {
        val key = BlockPos.asLong(x, y, z)
        val cached = this.heights.get(key)
        if (!cached.isNaN()) {
            return cached
        }
        val half = this.width / 2.0
        val height = this.sample(x, y, z, 0.5 - half, 0.5 - half, 0.5 + half, 0.5 + half)
        this.heights.put(key, height)
        return height
    }

    public fun getEntryHeight(x: Int, y: Int, z: Int, from: Direction): Double {
        val half = this.width / 2.0
        return when (from) {
            Direction.NORTH -> this.sample(x, y, z, 0.5 - half, 0.0, 0.5 + half, half)
            Direction.SOUTH -> this.sample(x, y, z, 0.5 - half, 1.0 - half, 0.5 + half, 1.0)
            Direction.WEST -> this.sample(x, y, z, 0.0, 0.5 - half, half, 0.5 + half)
            Direction.EAST -> this.sample(x, y, z, 1.0 - half, 0.5 - half, 1.0, 0.5 + half)
            else -> this.getStandHeight(x, y, z)
        }
    }

    public fun getSurface(x: Int, y: Int, z: Int): Double {
        val stand = this.getStandHeight(x, y, z)
        return if (stand == NO_SUPPORT) NO_SUPPORT else y + stand
    }

    public fun findSupport(x: Int, z: Int, maxHeight: Double, minHeight: Double): Int {
        val top = Mth.clamp(Mth.floor(maxHeight), this.minY, this.maxY)
        val bottom = Mth.clamp(Mth.floor(minHeight) - 1, this.minY, this.maxY)
        for (y in top downTo bottom) {
            if (this.getBlock(x, y, z).isCollisionEmpty) {
                continue
            }
            val surface = this.getSurface(x, y, z)
            if (surface !in minHeight..maxHeight) {
                continue
            }
            if (!this.hasClearance(x, z, surface)) {
                continue
            }
            return y
        }
        return NO_BLOCK
    }

    public fun hasClearance(x: Int, z: Int, surface: Double): Boolean {
        val key = clearanceKey(x, z, surface)
        val cached = this.clearances.get(key)
        if (cached.toInt() != 0) {
            return cached.toInt() > 0
        }
        val clear = this.level.noBlockCollision(this.player, this.boundingBoxAt(x, z, surface))
        this.clearances.put(key, (if (clear) 1 else -1).toByte())
        return clear
    }

    public fun boundingBoxAt(x: Int, z: Int, surface: Double): AABB {
        val half = this.width / 2.0 - CLEARANCE_EPSILON
        val centreX = x + 0.5
        val centreZ = z + 0.5
        return AABB(
            centreX - half,
            surface + CLEARANCE_EPSILON,
            centreZ - half,
            centreX + half,
            surface + this.height - CLEARANCE_EPSILON,
            centreZ + half
        )
    }

    public fun isSubmerged(x: Int, z: Int, surface: Double): Boolean {
        val eye = Mth.floor(surface + this.height * 0.85)
        return this.getBlock(x, eye, z).isWater
    }

    public fun isDamaging(x: Int, z: Int, surface: Double): Boolean {
        val bottom = Mth.floor(surface - CLEARANCE_EPSILON)
        val top = Mth.floor(surface + this.height)
        for (y in bottom..top) {
            if (this.getBlock(x, y, z).isDamaging) {
                return true
            }
        }
        return false
    }

    public fun isClimbable(x: Int, y: Int, z: Int): Boolean {
        val info = this.getBlock(x, y, z)
        if (info.isClimbable) {
            return true
        }
        val state = info.state
        if (state.block !is TrapDoorBlock || !state.getValue(TrapDoorBlock.OPEN)) {
            return false
        }
        val below = this.getBlock(x, y - 1, z).state
        return below.isOf(Blocks.LADDER) &&
            below.getValue(LadderBlock.FACING) == state.getValue(TrapDoorBlock.FACING)
    }

    private fun sample(x: Int, y: Int, z: Int, minX: Double, minZ: Double, maxX: Double, maxZ: Double): Double {
        val info = this.getBlock(x, y, z)
        if (info.isFullCube) {
            return 1.0
        }
        val shape = info.shape
        if (shape.isEmpty) {
            return NO_SUPPORT
        }
        var top = NO_SUPPORT
        for (i in 0..SAMPLES) {
            val sampleX = Mth.clamp(Mth.lerp(i / SAMPLES.toDouble(), minX, maxX), 0.0, 1.0)
            for (j in 0..SAMPLES) {
                val sampleZ = Mth.clamp(Mth.lerp(j / SAMPLES.toDouble(), minZ, maxZ), 0.0, 1.0)
                // VoxelShape#max takes the remaining axes in cycle order, which for Y is (z, x)
                val sampled = shape.max(Direction.Axis.Y, sampleZ, sampleX)
                if (sampled > top) {
                    top = sampled
                }
            }
        }
        return top
    }

    public companion object {
        public const val NO_SUPPORT: Double = Double.NEGATIVE_INFINITY
        public const val NO_BLOCK: Int = Int.MIN_VALUE

        private const val SAMPLES = 2
        private const val CLEARANCE_EPSILON = 1.0E-3
        private const val CLEARANCE_PRECISION = 16.0
        private const val INITIAL_BLOCKS = 1024

        private fun clearanceKey(x: Int, z: Int, surface: Double): Long {
            return PathNode.columnKey(x, z, surface, CLEARANCE_PRECISION)
        }
    }
}
