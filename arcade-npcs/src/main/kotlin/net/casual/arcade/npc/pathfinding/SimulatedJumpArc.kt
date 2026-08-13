/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.pathfinding

import net.minecraft.util.Mth

public class SimulatedJumpArc internal constructor(
    private val travel: DoubleArray,
    private val heights: DoubleArray
) {
    public val apex: Double = this.heights.max()

    public val reach: Double = this.travel.last()

    public fun ascentTo(height: Double): Double {
        if (height <= 0.0) {
            return 0.0
        }
        for (i in 1 until this.heights.size) {
            if (this.heights[i] >= height) {
                return this.interpolate(i, height)
            }
        }
        return Double.NaN
    }

    public fun descentTo(height: Double): Double {
        if (height > this.apex) {
            return Double.NaN
        }
        var falling = false
        for (i in 1 until this.heights.size) {
            if (this.heights[i] < this.heights[i - 1]) {
                falling = true
            }
            if (falling && this.heights[i] <= height) {
                return this.interpolate(i, height)
            }
        }
        return Double.NaN
    }

    private fun interpolate(index: Int, height: Double): Double {
        val from = this.heights[index - 1]
        val to = this.heights[index]
        if (from == to) {
            return this.travel[index]
        }
        return Mth.lerp(Mth.inverseLerp(height, from, to), this.travel[index - 1], this.travel[index])
    }
}
