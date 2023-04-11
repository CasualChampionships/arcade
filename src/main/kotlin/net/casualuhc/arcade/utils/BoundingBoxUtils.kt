package net.casualuhc.arcade.utils

import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3

object BoundingBoxUtils {
    fun above(y: Double): AABB {
        return AABB(
            Double.MIN_VALUE,
            y,
            Double.MIN_VALUE,
            Double.MAX_VALUE,
            Double.MAX_VALUE,
            Double.MAX_VALUE
        )
    }

    fun around(position: Vec3, radius: Double): AABB {
        return AABB(
            position.x - radius,
            position.y - radius,
            position.z - radius,
            position.x + radius,
            position.y + radius,
            position.z + radius
        )
    }
}