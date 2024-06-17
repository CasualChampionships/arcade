package net.casual.arcade.utils

import net.casual.arcade.utils.location.Location
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import net.casual.arcade.utils.PlayerUtils.teleportTo as teleportPlayerTo

public object EntityUtils {
    @JvmStatic
    public fun Entity.teleportTo(location: Location) {
        if (this is ServerPlayer) {
            this.teleportPlayerTo(location)
            return
        }
        this.teleportTo(
            location.level,
            location.x,
            location.y,
            location.z,
            setOf(),
            Mth.wrapDegrees(location.yaw),
            Mth.wrapDegrees(location.pitch)
        )
    }
}