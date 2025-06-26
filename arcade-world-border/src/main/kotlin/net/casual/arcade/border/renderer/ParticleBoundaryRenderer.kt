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
import net.casual.arcade.utils.ClientboundLevelParticlesPacket
import net.casual.arcade.utils.codec.CodecProvider
import net.minecraft.core.particles.DustParticleOptions
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import java.util.function.Consumer

public open class ParticleBoundaryRenderer(
    protected val shape: BoundaryShape,
    protected val particles: BoundaryParticles = BoundaryParticles.DEFAULT,
    protected val range: Double = 40.0,
    protected val pointsPerBlock: Double = 0.25
): BoundaryRenderer {
    override fun render(players: Collection<ServerPlayer>) {
        if (players.isEmpty()) {
            return
        }
        val particle = when (this.shape.getStatus()) {
            BoundaryShape.Status.Stationary -> this.particles.stationary
            BoundaryShape.Status.Shrinking -> this.particles.shrinking
            BoundaryShape.Status.Growing -> this.particles.growing
        }
        for (point in this.shape.getPoints().iterator(this.pointsPerBlock)) {
            val packet = ClientboundLevelParticlesPacket(
                particle, point, alwaysRender = true, overrideLimiter = true
            )
            for (player in players) {
                if (player.position().closerThan(point, this.range)) {
                    player.connection.send(packet)
                }
            }
        }
    }

    override fun startRendering(player: ServerPlayer) {

    }

    override fun stopRendering(player: ServerPlayer) {

    }

    override fun restartRendering(player: ServerPlayer, sender: Consumer<Packet<ClientGamePacketListener>>) {

    }

    override fun factory(): BoundaryRenderer.Factory {
        return Factory(this.particles, this.range, this.pointsPerBlock)
    }

    public data class BoundaryParticles(
        val stationary: ParticleOptions,
        val shrinking: ParticleOptions,
        val growing: ParticleOptions
    ) {
        public companion object {
            public val DEFAULT: BoundaryParticles = BoundaryParticles(
                DustParticleOptions(0x20A0FF, 1.0F),
                DustParticleOptions(0xFF3030, 1.0F),
                DustParticleOptions(0x40FF80, 1.0F)
            )

            public val CODEC: Codec<BoundaryParticles> = RecordCodecBuilder.create { instance ->
                instance.group(
                    ParticleTypes.CODEC.fieldOf("stationary").forGetter(BoundaryParticles::stationary),
                    ParticleTypes.CODEC.fieldOf("shrinking").forGetter(BoundaryParticles::shrinking),
                    ParticleTypes.CODEC.fieldOf("growing").forGetter(BoundaryParticles::growing)
                ).apply(instance, ::BoundaryParticles)
            }
        }
    }

    public class Factory(
        private val particles: BoundaryParticles,
        private val range: Double,
        private val pointsPerBlock: Double
    ): BoundaryRenderer.Factory {
        override fun create(shape: BoundaryShape): BoundaryRenderer {
            return ParticleBoundaryRenderer(shape, this.particles, this.range, this.pointsPerBlock)
        }

        override fun codec(): MapCodec<out BoundaryRenderer.Factory> {
            return CODEC
        }

        public companion object: CodecProvider<Factory> {
            override val ID: ResourceLocation = ArcadeUtils.id("particle_border_renderer")

            override val CODEC: MapCodec<out Factory> = RecordCodecBuilder.mapCodec { instance ->
                instance.group(
                    BoundaryParticles.CODEC.fieldOf("particles").forGetter(Factory::particles),
                    Codec.DOUBLE.fieldOf("range").forGetter(Factory::range),
                    Codec.DOUBLE.fieldOf("points_per_block").forGetter(Factory::pointsPerBlock)
                ).apply(instance, ::Factory)
            }
        }
    }
}