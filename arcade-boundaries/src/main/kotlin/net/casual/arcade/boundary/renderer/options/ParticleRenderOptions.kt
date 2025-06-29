/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.boundary.renderer.options

import com.mojang.serialization.Codec
import net.casual.arcade.boundary.renderer.ParticleBoundaryRenderer
import net.casual.arcade.boundary.shape.BoundaryShape
import net.casual.arcade.boundary.utils.BoundaryRegistries
import net.casual.arcade.utils.ArcadeUtils
import net.minecraft.core.Registry
import net.minecraft.core.particles.DustParticleOptions
import net.minecraft.core.particles.ParticleOptions

/**
 * Provides options for [ParticleBoundaryRenderer]s.
 */
public interface ParticleRenderOptions {
    /**
     * Gets the [ParticleOptions] for the given [shape].
     *
     * @return The particle options.
     */
    public fun get(shape: BoundaryShape): ParticleOptions

    /**
     * Provides constant options for [ParticleRenderOptions].
     *
     * @param stationary The stationary particle options.
     * @param shrinking The shrinking particle options.
     * @param growing The growing particle options.
     */
    public class Constant(
        public val stationary: ParticleOptions,
        public val shrinking: ParticleOptions,
        public val growing: ParticleOptions
    ): ParticleRenderOptions {
        override fun get(shape: BoundaryShape): ParticleOptions {
            return shape.getStatus().choose(this.stationary, this.shrinking, this.growing)
        }
    }

    public companion object {
        /**
         * Default particle render options, uses redstone dust particles
         * with the colors of the vanilla border statuses.
         */
        public val DEFAULT: ParticleRenderOptions = this.register(
            "default",
            Constant(
                DustParticleOptions(0x20A0FF, 1.0F),
                DustParticleOptions(0xFF3030, 1.0F),
                DustParticleOptions(0x40FF80, 1.0F)
            )
        )

        public val CODEC: Codec<ParticleRenderOptions> = Codec.lazyInitialized {
            BoundaryRegistries.PARTICLE_RENDER_OPTIONS.byNameCodec()
        }

        internal fun bootstrap() {

        }

        @Suppress("SameParameterValue")
        private fun register(path: String, options: ParticleRenderOptions): ParticleRenderOptions {
            return Registry.register(BoundaryRegistries.PARTICLE_RENDER_OPTIONS, ArcadeUtils.id(path), options)
        }
    }
}