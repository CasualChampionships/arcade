package net.casual.arcade.utils

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

    public fun Double.wholeOrNull(): Int? {
        if (ceil(this) == floor(this)) {
            return this.toInt()
        }
        return null
    }
}