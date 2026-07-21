/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.math.location

import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.Relative
import net.minecraft.world.level.Level
import net.minecraft.world.level.portal.TeleportTransition
import net.minecraft.world.level.portal.TeleportTransition.PostTeleportTransition
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3

public val Entity.location: Location
    get() = Location(this.position(), this.rotationVector)

public val Entity.locationWithLevel: LocationWithLevel<Level>
    get() = LocationWithLevel(this.location, this.level())

public val ServerPlayer.locationWithLevel: LocationWithLevel<ServerLevel>
    get() = LocationWithLevel(this.location, this.level())

public fun Vec3.with(rotation: Vec2): Location {
    return Location(this, rotation)
}

public fun <L: Level> L.asLocation(
    position: Vec3 = Vec3.ZERO,
    rotation: Vec2 = Vec2.ZERO
): LocationWithLevel<L> {
    return LocationWithLevel(Location(position, rotation), this)
}

public fun LocationWithLevel<ServerLevel>.asTeleportTransition(
    velocity: Vec3 = Vec3.ZERO,
    missingRespawnBlock: Boolean = false,
    asPassenger: Boolean = false,
    relatives: Set<Relative> = setOf(),
    transition: PostTeleportTransition = TeleportTransition.DO_NOTHING
): TeleportTransition {
    return TeleportTransition(
        this.level,
        this.position,
        velocity,
        this.yRot,
        this.xRot,
        missingRespawnBlock,
        asPassenger,
        relatives,
        transition
    )
}

public fun Location.closerThan(other: Vec3, distance: Double): Boolean {
    return this.position.closerThan(other, distance)
}

public fun Location.closerThan(other: Location, distance: Double): Boolean {
    return this.position.closerThan(other.position, distance)
}

public fun LocationWithLevel<*>.closerThan(other: Location, distance: Double): Boolean {
    return this.location.closerThan(other, distance)
}

public fun LocationWithLevel<*>.closerThan(other: Vec3, distance: Double): Boolean {
    return this.location.closerThan(other, distance)
}

public fun Location.closerThan(other: LocationWithLevel<*>, distance: Double): Boolean {
    return this.closerThan(other.location, distance)
}

public fun LocationWithLevel<*>.closerThan(other: LocationWithLevel<*>, distance: Double): Boolean {
    return this.level == other.level && this.location.closerThan(other.location, distance)
}