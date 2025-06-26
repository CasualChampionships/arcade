/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.shapes.impl

import net.casual.arcade.visuals.shapes.ShapePoints
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.phys.Vec3

public class LevelSurfaceShape(
    private val level: ServerLevel,
    private val wrapped: ShapePoints
): ShapePoints {
    override fun iterator(pointsPerUnit: Double): Iterator<Vec3> {
        return LevelSurfaceIterator(this.level, this.wrapped.iterator(pointsPerUnit))
    }

    private class LevelSurfaceIterator(
        private val level: ServerLevel,
        private val wrapped: Iterator<Vec3>
    ): Iterator<Vec3> {
        override fun hasNext(): Boolean {
            return this.wrapped.hasNext()
        }

        override fun next(): Vec3 {
            val point = this.wrapped.next()
            val pos = BlockPos.containing(point)
            val chunk = this.level.getChunk(pos)
            val height = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, pos.x, pos.y)
            return Vec3(point.x, height + 1.0, point.z)
        }
    }
}