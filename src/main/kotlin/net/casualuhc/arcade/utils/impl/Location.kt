package net.casualuhc.arcade.utils.impl

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
}