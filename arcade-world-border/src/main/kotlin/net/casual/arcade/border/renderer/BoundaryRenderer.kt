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
    public fun render(players: Collection<ServerPlayer>)

    public fun startRendering(player: ServerPlayer)

    public fun stopRendering(player: ServerPlayer)

    public fun restartRendering(player: ServerPlayer, sender: Consumer<Packet<ClientGamePacketListener>>)

    public fun factory(): Factory

    public interface Factory {
        public fun create(shape: BoundaryShape): BoundaryRenderer

        public fun codec(): MapCodec<out Factory>

        public companion object {
            public val CODEC: Codec<Factory> = Codec.lazyInitialized {
                BorderRegistries.BOUNDARY_RENDERER_FACTORY.byNameCodec()
                    .dispatch(Factory::codec, Function.identity())
            }

            internal fun bootstrap(registry: Registry<MapCodec<out Factory>>) {
                AsyncParticleBoundaryRenderer.Factory.register(registry)
                AxisAlignedDisplayBoundaryRenderer.Factory.register(registry)
                ParticleBoundaryRenderer.Factory.register(registry)
            }
        }
    }
}