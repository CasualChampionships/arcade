package net.casual.arcade.gui.shapes

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.phys.Vec3

public class LevelSurfaceShape(
    private val level: ServerLevel,
    private val wrapped: ShapePoints
): ShapePoints {
    /**
     * Returns an iterator with a specified number of steps
     * between the points.
     *
     * @param steps The number of steps to take.
     * @return An [Iterator] with elements of [Vec3].
     */
    override fun iterator(steps: Int): Iterator<Vec3> {
        return LevelSurfaceIterator(this.level, this.wrapped.iterator(steps))
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