package net.casual.arcade.minigame.events.lobby

import kotlinx.serialization.Serializable
import net.casual.arcade.utils.impl.Location
import net.minecraft.server.level.ServerLevel

@Serializable
public class SpawnConfig(
    public val x: Double = 0.0,
    public val y: Double = 1.0,
    public val z: Double = 0.0,
    public val yaw: Float = 0.0F,
    public val pitch: Float = 0.0F
) {
    public fun location(level: ServerLevel): Location {
        return Location.of(this.x, this.y, this.z, this.yaw, this.pitch, level)
    }

    public companion object {
        public val DEFAULT: SpawnConfig = SpawnConfig()
    }
}