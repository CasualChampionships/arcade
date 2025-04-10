/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.math.location

import net.casual.arcade.utils.math.location.Location.Companion.location
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.Relative
import net.minecraft.world.level.Level
import net.minecraft.world.level.portal.TeleportTransition
import net.minecraft.world.level.portal.TeleportTransition.PostTeleportTransition
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3

public data class LocationWithLevel<L: Level>(
    public val location: Location,
    public val level: L
) {
    val position: Vec3 get() = this.location.position
    val rotation: Vec2 get() = this.location.rotation

    public val x: Double get() = this.location.x
    public val y: Double get() = this.location.y
    public val z: Double get() = this.location.z

    public val xRot: Float get() = this.location.xRot
    public val yRot: Float get() = this.location.yRot

    public fun server(): LocationWithLevel<ServerLevel> {
        @Suppress("UNCHECKED_CAST")
        return this as LocationWithLevel<ServerLevel>
    }

    public companion object {
        public val Entity.locationWithLevel: LocationWithLevel<Level>
            get() = LocationWithLevel(this.location, this.level())

        public val ServerPlayer.locationWithLevel: LocationWithLevel<ServerLevel>
            get() = LocationWithLevel(this.location, this.serverLevel())

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
    }
}