package net.casual.arcade.utils

import net.minecraft.core.Direction8
import net.minecraft.world.phys.Vec3
import kotlin.math.ceil
import kotlin.math.floor

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
        if (ceil(this) == floor(this)) {
            return this.toInt()
        }
        return null
    }
}