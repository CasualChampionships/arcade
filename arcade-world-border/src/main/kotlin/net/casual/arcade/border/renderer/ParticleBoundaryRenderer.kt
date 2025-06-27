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
import net.casual.arcade.utils.ClientboundLevelParticlesPacket
import net.casual.arcade.utils.codec.CodecProvider
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import java.util.function.Consumer

public open class ParticleBoundaryRenderer(
    protected val shape: BoundaryShape,
    protected val particles: ParticleRenderOptions = ParticleRenderOptions.DEFAULT,
    protected val range: Double = 40.0,
    protected val pointsPerBlock: Double = 0.25
): BoundaryRenderer {
    override fun render(players: Collection<ServerPlayer>) {
        if (players.isEmpty()) {
            return
        }
        val particle = this.particles.get(this.shape)
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

    public class Factory(
        private val particles: ParticleRenderOptions,
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
                    ParticleRenderOptions.CODEC.fieldOf("particles").forGetter(Factory::particles),
                    Codec.DOUBLE.fieldOf("range").forGetter(Factory::range),
                    Codec.DOUBLE.fieldOf("points_per_block").forGetter(Factory::pointsPerBlock)
                ).apply(instance, ::Factory)
            }
        }
    }
}