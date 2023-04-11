package net.casualuhc.arcade.math

import net.casualuhc.arcade.utils.LevelUtils
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3

@Suppress("unused")
class Location(
    val level: ServerLevel,
    val position: Vec3,
    val rotation: Vec2
) {
    val x get() = this.position.x
    val y get() = this.position.y
    val z get() = this.position.z

    val yaw get() = this.rotation.x
    val pitch get() = this.rotation.y

    companion object {
        fun of(): Location {
            return Location(LevelUtils.overworld(), Vec3.ZERO, Vec2.ZERO)
        }

        fun of(position: Vec3, rotation: Vec2 = Vec2.ZERO, level: ServerLevel = LevelUtils.overworld()): Location {
            return Location(level, position, rotation)
        }

        fun of(x: Double, y: Double, z: Double, yaw: Float = 0.0F, pitch: Float = 0.0F, level: ServerLevel): Location {
            return Location(level, Vec3(x, y, z), Vec2(yaw, pitch))
        }
    }
}