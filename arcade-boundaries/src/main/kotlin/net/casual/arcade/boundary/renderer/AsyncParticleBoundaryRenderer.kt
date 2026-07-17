/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.boundary.renderer

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.boundary.renderer.options.ParticleRenderOptions
import net.casual.arcade.boundary.shape.BoundaryShape
import net.casual.arcade.observer.Observer
import net.casual.arcade.utils.arcade
import net.casual.arcade.utils.serialization.codec.CodecProvider
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.Util
import java.util.concurrent.CopyOnWriteArraySet

/**
 * Extension of [ParticleBoundaryRenderer] that updates asynchronously
 * as generating the particle positions can be expensive.
 *
 * @param shape The shape to render.
 * @param particles The particle render options.
 * @param range The range at which to display particles to the player.
 * @param particlesPerBlock The number of particles to display per block.
 * @see ParticleBoundaryRenderer
 */
public class AsyncParticleBoundaryRenderer(
    level: ServerLevel,
    shape: BoundaryShape,
    particles: ParticleRenderOptions = ParticleRenderOptions.DEFAULT,
    range: Double = 40.0,
    particlesPerBlock: Double = 0.25
): ParticleBoundaryRenderer(level, shape, particles, range, particlesPerBlock) {
    override fun render() {
        if (this.observers.isNotEmpty()) {
            Util.ioPool().execute { super.render() }
        }
    }

    override fun factory(): BoundaryRenderer.Factory {
        return Factory(this.particles, this.range, this.particlesPerBlock)
    }

    override fun createObserverSet(): MutableSet<Observer> {
        return CopyOnWriteArraySet()
    }

    public open class Factory(
        private val particles: ParticleRenderOptions,
        private val range: Double,
        private val pointsPerBlock: Double
    ): BoundaryRenderer.Factory {
        override fun create(level: ServerLevel, shape: BoundaryShape): BoundaryRenderer {
            return AsyncParticleBoundaryRenderer(level, shape, this.particles, this.range, this.pointsPerBlock)
        }

        override fun codec(): MapCodec<out BoundaryRenderer.Factory> {
            return codec
        }

        public companion object: CodecProvider<Factory> {
            override val id: Identifier = arcade("async_particle_border_renderer")

            override val codec: MapCodec<out Factory> = RecordCodecBuilder.mapCodec { instance ->
                instance.group(
                    ParticleRenderOptions.CODEC.fieldOf("particles").forGetter(Factory::particles),
                    Codec.DOUBLE.fieldOf("range").forGetter(Factory::range),
                    Codec.DOUBLE.fieldOf("points_per_block").forGetter(Factory::pointsPerBlock)
                ).apply(instance, ::Factory)
            }
        }
    }
}