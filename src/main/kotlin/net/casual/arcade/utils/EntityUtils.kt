package net.casual.arcade.utils

import net.casual.arcade.utils.PlayerUtils.teleportTo as teleportPlayerTo
import net.casual.arcade.utils.location.Location
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity

public object EntityUtils {
    @JvmStatic
    public fun Entity.teleportTo(location: Location) {
        if (this is ServerPlayer) {
            this.teleportPlayerTo(location)
            return
        }
        this.teleportTo(location.level, location.x, location.y, location.z, setOf(), location.yaw, location.pitch)
    }
}