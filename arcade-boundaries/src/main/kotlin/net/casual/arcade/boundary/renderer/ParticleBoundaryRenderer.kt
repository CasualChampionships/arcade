/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.boundary.renderer

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.casual.arcade.boundary.renderer.options.ParticleRenderOptions
import net.casual.arcade.boundary.shape.BoundaryShape
import net.casual.arcade.networking.observer.Observer
import net.casual.arcade.utils.ClientboundLevelParticlesPacket
import net.casual.arcade.utils.arcade
import net.casual.arcade.utils.math.location.closerThan
import net.casual.arcade.utils.serialization.codec.CodecProvider
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerLevel

/**
 * Implementation of [BoundaryRenderer] that renders the boundary
 * as particles to the player.
 *
 * @param shape The shape to render.
 * @param particles The particle render options.
 * @param range The range at which to display particles to the player.
 * @param particlesPerBlock The number of particles to display per block.
 * @see AsyncParticleBoundaryRenderer
 */
public open class ParticleBoundaryRenderer(
    protected val level: ServerLevel,
    protected val shape: BoundaryShape,
    protected val particles: ParticleRenderOptions = ParticleRenderOptions.DEFAULT,
    protected val range: Double = 40.0,
    protected val particlesPerBlock: Double = 0.25
): BoundaryRenderer {
    protected val observers: MutableSet<Observer> = this.createObserverSet()

    override fun render() {
        if (this.observers.isEmpty()) {
            return
        }
        val particle = this.particles.get(this.shape)
        for (point in this.shape.getPoints().iterator(this.particlesPerBlock)) {
            val packet = ClientboundLevelParticlesPacket(
                particle, point, alwaysRender = true, overrideLimiter = true
            )
            for (observer in this.observers) {
                if (observer.location.closerThan(point, this.range)) {
                    observer.send(packet)
                }
            }
        }
    }

    override fun factory(): BoundaryRenderer.Factory {
        return Factory(this.particles, this.range, this.particlesPerBlock)
    }

    override fun addObserver(observer: Observer) {
        this.observers.add(observer)
    }

    override fun removeObserver(observer: Observer) {
        this.observers.remove(observer)
    }

    protected open fun createObserverSet(): MutableSet<Observer> {
        return ObjectOpenHashSet()
    }

    public class Factory(
        private val particles: ParticleRenderOptions,
        private val range: Double,
        private val pointsPerBlock: Double
    ): BoundaryRenderer.Factory {
        override fun create(level: ServerLevel, shape: BoundaryShape): BoundaryRenderer {
            return ParticleBoundaryRenderer(level, shape, this.particles, this.range, this.pointsPerBlock)
        }

        override fun codec(): MapCodec<out BoundaryRenderer.Factory> {
            return codec
        }

        public companion object: CodecProvider<Factory> {
            override val id: Identifier = arcade("particle_border_renderer")

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