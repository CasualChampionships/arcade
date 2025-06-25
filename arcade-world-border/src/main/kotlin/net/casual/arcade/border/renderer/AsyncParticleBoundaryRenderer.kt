/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.border.renderer

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.border.shape.BoundaryShape
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.codec.CodecProvider
import net.minecraft.Util
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer

public class AsyncParticleBoundaryRenderer(
    particle: ParticleOptions,
    range: Double
): ParticleBoundaryRenderer(particle, range) {
    override fun render(shape: BoundaryShape, players: Collection<ServerPlayer>) {
        if (players.isNotEmpty()) {
            Util.ioPool().execute { super.render(shape, players) }
        }
    }

    override fun codec(): MapCodec<out BoundaryRenderer> {
        return CODEC
    }

    public companion object: CodecProvider<AsyncParticleBoundaryRenderer> {
        override val ID: ResourceLocation = ArcadeUtils.id("async_particle_boundary_renderer")
        override val CODEC: MapCodec<out AsyncParticleBoundaryRenderer> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                ParticleTypes.CODEC.fieldOf("particle_type").forGetter(AsyncParticleBoundaryRenderer::particle),
                Codec.DOUBLE.fieldOf("range").forGetter(AsyncParticleBoundaryRenderer::range)
            ).apply(instance, ::AsyncParticleBoundaryRenderer)
        }

    }
}