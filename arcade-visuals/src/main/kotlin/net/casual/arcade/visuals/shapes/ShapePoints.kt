/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.shapes

import net.casual.arcade.utils.PlayerUtils.sendParticles
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.phys.Vec3

/**
 * This interface represents a traversable shape which
 * can be iterated and then displayed with particles.
 */
public fun interface ShapePoints: Iterable<Vec3> {
    /**
     * Returns an iterator with the default step of `1`.
     *
     * @return An [Iterator] with elements of [Vec3].
     */
    override fun iterator(): Iterator<Vec3> {
        return this.iterator(1)
    }

    /**
     * Returns an iterator with a specified number of steps
     * between the points.
     *
     * @param steps The number of steps to take.
     * @return An [Iterator] with elements of [Vec3].
     */
    public fun iterator(steps: Int): Iterator<Vec3>

    public companion object {
        public fun ShapePoints.drawAsParticlesFor(
            player: ServerPlayer,
            particle: ParticleOptions = ParticleTypes.END_ROD,
            steps: Int = 10
        ) {
            for (point in this.iterator(steps)) {
                player.sendParticles(particle, point)
            }
        }
    }
}