package net.casual.arcade.utils.impl

import net.casual.arcade.utils.LevelUtils
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3

public data class Location(
    val level: ServerLevel,
    val position: Vec3,
    val rotation: Vec2
) {
    val x: Double get() = this.position.x
    val y: Double get() = this.position.y
    val z: Double get() = this.position.z

    val pitch: Float get() = this.rotation.x
    val yaw: Float get() = this.rotation.y

    public companion object {
        public fun of(): Location {
            return Location(LevelUtils.overworld(), Vec3.ZERO, Vec2.ZERO)
        }

        public fun of(position: Vec3, rotation: Vec2 = Vec2.ZERO, level: ServerLevel = LevelUtils.overworld()): Location {
            return Location(level, position, rotation)
        }

        public fun of(x: Double, y: Double, z: Double, yaw: Float = 0.0F, pitch: Float = 0.0F, level: ServerLevel): Location {
            return Location(level, Vec3(x, y, z), Vec2(yaw, pitch))
        }
    }
}