package net.casual.arcade.utils

import net.casual.arcade.utils.location.Location
import net.minecraft.world.entity.Entity

public object EntityUtils {
    @JvmStatic
    public fun Entity.teleportTo(location: Location) {
        this.teleportTo(location.level, location.x, location.y, location.z, setOf(), location.yaw, location.pitch)
    }
}