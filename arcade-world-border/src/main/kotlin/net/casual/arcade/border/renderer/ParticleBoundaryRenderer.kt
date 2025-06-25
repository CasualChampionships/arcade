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
import net.casual.arcade.utils.PlayerUtils.broadcast
import net.casual.arcade.utils.codec.CodecProvider
import net.minecraft.Util
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import java.util.function.Consumer
import kotlin.time.Duration.Companion.milliseconds

public open class ParticleBoundaryRenderer(
    protected val particle: ParticleOptions,
    protected val range: Double
): BoundaryRenderer {
    override fun render(shape: BoundaryShape, players: Collection<ServerPlayer>) {
        if (players.isEmpty()) {
            return
        }
        for (point in shape.getPoints()) {
            val packet = ClientboundLevelParticlesPacket(
                this.particle, point, alwaysRender = true, overrideLimiter = true
            )
            for (player in players) {
                if (player.position().closerThan(point, this.range)) {
                    player.connection.send(packet)
                }
            }
        }
    }

    override fun startRendering(shape: BoundaryShape, player: ServerPlayer) {

    }

    override fun stopRendering(shape: BoundaryShape, player: ServerPlayer) {

    }

    override fun restartRendering(
        shape: BoundaryShape,
        player: ServerPlayer,
        sender: Consumer<Packet<ClientGamePacketListener>>
    ) {

    }

    override fun codec(): MapCodec<out BoundaryRenderer> {
        return CODEC
    }

    public companion object: CodecProvider<ParticleBoundaryRenderer> {
        override val ID: ResourceLocation = ArcadeUtils.id("particle_border_renderer")

        override val CODEC: MapCodec<out ParticleBoundaryRenderer> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                ParticleTypes.CODEC.fieldOf("particle_type").forGetter(ParticleBoundaryRenderer::particle),
                Codec.DOUBLE.fieldOf("range").forGetter(ParticleBoundaryRenderer::range)
            ).apply(instance, ::ParticleBoundaryRenderer)
        }
    }
}