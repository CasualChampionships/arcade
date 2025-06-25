/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.border.renderer

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import net.casual.arcade.border.shape.BoundaryShape
import net.casual.arcade.border.utils.BorderRegistries
import net.casual.arcade.utils.codec.CodecProvider.Companion.register
import net.minecraft.core.Registry
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.server.level.ServerPlayer
import java.util.function.Consumer
import java.util.function.Function

public interface BoundaryRenderer {
    public fun render(shape: BoundaryShape, players: Collection<ServerPlayer>)

    public fun startRendering(shape: BoundaryShape, player: ServerPlayer)

    public fun stopRendering(shape: BoundaryShape, player: ServerPlayer)

    public fun restartRendering(
        shape: BoundaryShape,
        player: ServerPlayer,
        sender: Consumer<Packet<ClientGamePacketListener>>
    )

    public fun codec(): MapCodec<out BoundaryRenderer>

    public companion object {
        public val CODEC: Codec<BoundaryRenderer> = Codec.lazyInitialized {
            BorderRegistries.BOUNDARY_RENDERER.byNameCodec()
                .dispatch(BoundaryRenderer::codec, Function.identity())
        }

        internal fun bootstrap(registry: Registry<MapCodec<out BoundaryRenderer>>) {
            AsyncParticleBoundaryRenderer.register(registry)
            ParticleBoundaryRenderer.register(registry)
        }
    }
}