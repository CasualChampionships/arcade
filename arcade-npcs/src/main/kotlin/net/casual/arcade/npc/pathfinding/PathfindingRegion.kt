/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.pathfinding

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.minecraft.core.BlockPos
import net.minecraft.core.SectionPos
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.CollisionGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.border.WorldBorder
import net.minecraft.world.level.chunk.ChunkAccess
import net.minecraft.world.level.material.FluidState
import net.minecraft.world.level.material.Fluids
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.shapes.VoxelShape

// Essentially just PathNavigationRegion but lazy
public class PathfindingRegion(public val level: Level): CollisionGetter {
    private val chunks = Long2ObjectOpenHashMap<ChunkAccess?>()

    override fun getWorldBorder(): WorldBorder {
        return this.level.worldBorder
    }

    override fun getChunkForCollisions(chunkX: Int, chunkZ: Int): BlockGetter? {
        return this.getChunk(chunkX, chunkZ)
    }

    override fun getEntityCollisions(source: Entity?, testArea: AABB): List<VoxelShape> {
        return listOf()
    }

    override fun getBlockEntity(pos: BlockPos): BlockEntity? {
        return this.getChunk(pos)?.getBlockEntity(pos)
    }

    override fun getBlockState(pos: BlockPos): BlockState {
        if (this.isOutsideBuildHeight(pos)) {
            return Blocks.AIR.defaultBlockState()
        }
        return this.getChunk(pos)?.getBlockState(pos) ?: Blocks.AIR.defaultBlockState()
    }

    override fun getFluidState(pos: BlockPos): FluidState {
        if (this.isOutsideBuildHeight(pos)) {
            return Fluids.EMPTY.defaultFluidState()
        }
        return this.getChunk(pos)?.getFluidState(pos) ?: Fluids.EMPTY.defaultFluidState()
    }

    override fun getMinY(): Int {
        return this.level.minY
    }

    override fun getHeight(): Int {
        return this.level.height
    }

    private fun getChunk(pos: BlockPos): ChunkAccess? {
        return this.getChunk(
            SectionPos.blockToSectionCoord(pos.x),
            SectionPos.blockToSectionCoord(pos.z)
        )
    }

    private fun getChunk(chunkX: Int, chunkZ: Int): ChunkAccess? {
        val key = ChunkPos.pack(chunkX, chunkZ)
        val cached = this.chunks.get(key)
        if (cached != null) {
            return cached
        }
        val chunk = this.level.chunkSource.getChunkNow(chunkX, chunkZ)
        if (chunk != null) {
            this.chunks.put(key, chunk)
        }
        return chunk
    }
}
