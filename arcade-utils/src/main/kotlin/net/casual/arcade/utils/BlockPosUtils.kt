package net.casual.arcade.utils

import net.minecraft.core.BlockPos
import net.minecraft.core.BlockPos.MutableBlockPos
import net.minecraft.core.Direction
import kotlin.math.cos
import kotlin.math.sin

public object BlockPosUtils {
    public fun dispersed(
        center: BlockPos,
        steps: Int,
        radius: Int,
        branches: Int,
        normal: Direction.Axis
    ): Iterable<MutableBlockPos> {
        return Iterable {
            DispersedBlockPosIterator(center, steps, radius, branches, normal)
        }
    }

    private class DispersedBlockPosIterator(
        val center: BlockPos,
        val steps: Int,
        val radius: Int,
        val branches: Int,
        val normal: Direction.Axis
    ): Iterator<MutableBlockPos> {
        val distance = 1.0 / this.steps.toDouble() * this.radius
        var current: MutableBlockPos = this.center.mutable()
        var step = 0

        override fun hasNext(): Boolean {
            return this.step < this.branches * this.steps
        }

        override fun next(): MutableBlockPos {
            val step = (this.step / this.branches) + 1
            val branch = this.step++ % this.branches
            val angle = 2 * Math.PI * branch / this.branches
            val ds = (step * this.distance * sin(angle)).toInt()
            val dc = (step * this.distance * cos(angle)).toInt()
            when (this.normal) {
                Direction.Axis.X -> this.current.setWithOffset(this.center, 0, ds, dc)
                Direction.Axis.Y -> this.current.setWithOffset(this.center, ds, 0, dc)
                Direction.Axis.Z -> this.current.setWithOffset(this.center, ds, dc, 0)
            }
            return this.current
        }
    }
}