package net.casual.arcade.utils

import net.minecraft.core.Direction8
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import kotlin.math.ceil

public object MathUtils {
    public operator fun Vec3.component1(): Double {
        return this.x
    }

    public operator fun Vec3.component2(): Double {
        return this.y
    }

    public operator fun Vec3.component3(): Double {
        return this.z
    }

    public operator fun Vec2.component1(): Float {
        return this.y
    }

    public operator fun Vec2.component2(): Float {
        return this.x
    }

    public fun Direction8.opposite(): Direction8 {
        return when (this) {
            Direction8.NORTH -> Direction8.SOUTH
            Direction8.NORTH_EAST -> Direction8.SOUTH_WEST
            Direction8.EAST -> Direction8.WEST
            Direction8.SOUTH_EAST -> Direction8.NORTH_WEST
            Direction8.SOUTH -> Direction8.NORTH
            Direction8.SOUTH_WEST -> Direction8.NORTH_EAST
            Direction8.WEST -> Direction8.EAST
            Direction8.NORTH_WEST -> Direction8.SOUTH_EAST
        }
    }

    public fun Double.wholeOrNull(): Int? {
        if (ceil(this) == this) {
            return this.toInt()
        }
        return null
    }

    public fun centeredScale(percent: Float, factor: Float): Float {
        val shift = (1 - factor) / 2.0F
        return shift + percent * factor
    }
}