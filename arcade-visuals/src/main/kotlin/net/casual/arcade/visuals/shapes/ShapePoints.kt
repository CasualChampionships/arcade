/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.shapes

import net.casual.arcade.utils.PlayerUtils.sendParticles
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.phys.Vec3

/**
 * This interface represents a traversable shape which
 * provides interpolated points along the surface of
 * the shape which can be iterated and then displayed
 * with particles.
 */
public fun interface ShapePoints: Iterable<Vec3> {
    /**
     * Returns an iterator with the default pointsPerUnit of `1.0`.
     *
     * @return An [Iterator] with elements of [Vec3].
     */
    override fun iterator(): Iterator<Vec3> {
        return this.iterator(1.0)
    }

    /**
     * Returns an iterator with a specified number of points
     * between each unit.
     *
     * @param pointsPerUnit The number of points per unit.
     * @return An [Iterator] with elements of [Vec3].
     */
    public fun iterator(pointsPerUnit: Double): Iterator<Vec3>

    public companion object {
        /**
         * This gets the raw points of the shape, without interpolation.
         *
         * @return An [Iterator] with elements of [Vec3].
         */
        public fun ShapePoints.points(): Iterator<Vec3> {
            return this.iterator(0.0)
        }

        public fun ShapePoints.drawAsParticlesFor(
            player: ServerPlayer,
            particle: ParticleOptions = ParticleTypes.END_ROD,
            pointsPerUnit: Double = 1.0
        ) {
            for (point in this.iterator(pointsPerUnit)) {
                player.sendParticles(particle, point)
            }
        }

        public fun ShapePoints.drawAsParticles(
            level: ServerLevel,
            particle: ParticleOptions = ParticleTypes.END_ROD,
            pointsPerUnit: Double = 1.0
        ) {
            for (point in this.iterator(pointsPerUnit)) {
                level.sendParticles(particle, point.x, point.y, point.z, 0, 0.0, 0.0, 0.0, 0.0)
            }
        }
    }
}