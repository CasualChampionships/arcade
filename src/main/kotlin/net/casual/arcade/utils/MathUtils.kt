package net.casual.arcade.utils

import net.minecraft.world.phys.Vec3

object MathUtils {
    operator fun Vec3.component1(): Double {
        return this.x
    }

    operator fun Vec3.component2(): Double {
        return this.y
    }

    operator fun Vec3.component3(): Double {
        return this.z
    }
}