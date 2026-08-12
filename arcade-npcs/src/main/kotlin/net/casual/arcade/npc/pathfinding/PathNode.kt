/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.pathfinding

import net.casual.arcade.npc.pathfinding.movement.MovementType
import net.minecraft.core.BlockPos
import net.minecraft.util.Mth
import net.minecraft.world.phys.Vec3
import kotlin.math.sqrt

public class PathNode(
    public val x: Int,
    public val z: Int,
    public val surface: Double
) {
    public val y: Int = Mth.floor(this.surface - SUPPORT_EPSILON)

    public var cost: Double = 0.0
    public var heuristic: Double = 0.0
    public var total: Double = 0.0

    public var heapIndex: Int = -1

    public var closed: Boolean = false

    public var previous: PathNode? = null
    public var previousType: MovementType? = null
    public var previousData: Int = 0
    public var previousCost: Double = 0.0

    private var cachedTarget: Vec3? = null

    public val target: Vec3
        get() {
            val cached = this.cachedTarget
            if (cached != null) {
                return cached
            }
            val target = Vec3(this.x + 0.5, this.surface, this.z + 0.5)
            this.cachedTarget = target
            return target
        }

    public val support: BlockPos
        get() = BlockPos(this.x, this.y, this.z)

    public val feet: BlockPos
        get() = BlockPos(this.x, Mth.floor(this.surface), this.z)

    public fun isOpen(): Boolean {
        return this.heapIndex >= 0
    }

    public fun distanceTo(other: PathNode): Double {
        return this.distanceTo(other.x + 0.5, other.surface, other.z + 0.5)
    }

    public fun distanceTo(x: Double, y: Double, z: Double): Double {
        val dx = x - (this.x + 0.5)
        val dy = y - this.surface
        val dz = z - (this.z + 0.5)
        return sqrt(dx * dx + dy * dy + dz * dz)
    }

    override fun toString(): String {
        return "NPCPathNode(${this.x}, ${this.y}, ${this.z}, surface=${this.surface})"
    }

    public companion object {
        private const val SUPPORT_EPSILON = 1.0E-4

        private const val HEIGHT_PRECISION = 64.0

        public fun key(x: Int, z: Int, surface: Double): Long {
            return columnKey(x, z, surface, HEIGHT_PRECISION)
        }

        internal fun columnKey(x: Int, z: Int, height: Double, precision: Double): Long {
            val quantised = (Mth.floor(height * precision) + HEIGHT_BIAS).toLong() and HEIGHT_MASK
            return (x.toLong() and COORDINATE_MASK) or
                ((z.toLong() and COORDINATE_MASK) shl COORDINATE_BITS) or
                (quantised shl (COORDINATE_BITS * 2))
        }

        private const val COORDINATE_BITS = 22
        private const val COORDINATE_MASK = 0x3FFFFFL
        private const val HEIGHT_BIAS = 1 shl 19
        private const val HEIGHT_MASK = 0xFFFFFL
    }
}
