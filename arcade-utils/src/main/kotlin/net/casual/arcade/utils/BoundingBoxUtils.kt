package net.casual.arcade.utils

import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3

public object BoundingBoxUtils {
    public fun above(y: Double): AABB {
        return AABB(
            Double.MIN_VALUE,
            y,
            Double.MIN_VALUE,
            Double.MAX_VALUE,
            Double.MAX_VALUE,
            Double.MAX_VALUE
        )
    }

    public fun around(position: Vec3, radius: Double): AABB {
        return AABB(
            position.x - radius,
            position.y - radius,
            position.z - radius,
            position.x + radius,
            position.y + radius,
            position.z + radius
        )
    }

    public fun AABB.getSizeVec(): Vec3 {
        return Vec3(this.xsize, this.ysize, this.zsize)
    }
}