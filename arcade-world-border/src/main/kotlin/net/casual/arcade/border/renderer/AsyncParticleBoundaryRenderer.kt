/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.border.renderer

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.border.renderer.options.ParticleRenderOptions
import net.casual.arcade.border.shape.BoundaryShape
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.codec.CodecProvider
import net.minecraft.Util
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer

public class AsyncParticleBoundaryRenderer(
    shape: BoundaryShape,
    particle: ParticleRenderOptions = ParticleRenderOptions.DEFAULT,
    range: Double = 40.0,
    pointsPerBlock: Double = 0.25
): ParticleBoundaryRenderer(shape, particle, range, pointsPerBlock) {
    override fun render(players: Collection<ServerPlayer>) {
        if (players.isNotEmpty()) {
            Util.ioPool().execute { super.render(players) }
        }
    }

    override fun factory(): BoundaryRenderer.Factory {
        return Factory(this.particles, this.range, this.pointsPerBlock)
    }

    public open class Factory(
        private val particles: ParticleRenderOptions,
        private val range: Double,
        private val pointsPerBlock: Double
    ): BoundaryRenderer.Factory {
        override fun create(shape: BoundaryShape): BoundaryRenderer {
            return AsyncParticleBoundaryRenderer(shape, this.particles, this.range, this.pointsPerBlock)
        }

        override fun codec(): MapCodec<out BoundaryRenderer.Factory> {
            return CODEC
        }

        public companion object: CodecProvider<Factory> {
            override val ID: ResourceLocation = ArcadeUtils.id("async_particle_border_renderer")

            override val CODEC: MapCodec<out Factory> = RecordCodecBuilder.mapCodec { instance ->
                instance.group(
                    ParticleRenderOptions.CODEC.fieldOf("particles").forGetter(Factory::particles),
                    Codec.DOUBLE.fieldOf("range").forGetter(Factory::range),
                    Codec.DOUBLE.fieldOf("points_per_block").forGetter(Factory::pointsPerBlock)
                ).apply(instance, ::Factory)
            }
        }
    }
}