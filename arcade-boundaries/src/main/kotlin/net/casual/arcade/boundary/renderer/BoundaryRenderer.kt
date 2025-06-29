/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.boundary.renderer

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import net.casual.arcade.boundary.shape.BoundaryShape
import net.casual.arcade.boundary.utils.BoundaryRegistries
import net.casual.arcade.utils.codec.CodecProvider.Companion.register
import net.minecraft.core.Registry
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import java.util.function.Consumer
import java.util.function.Function

/**
 * This provides an interface for rendering [BoundaryShape]s to players.
 *
 * @see BoundaryShape
 */
public interface BoundaryRenderer {
    /**
     * Updates the renderer, this is called every tick.
     *
     * @param level The level of the boundary.
     * @param players The players to render to.
     */
    public fun render(level: ServerLevel, players: Collection<ServerPlayer>)

    /**
     * Called when a player starts observing the boundary.
     *
     * @param player The player to initialize rendering for.
     */
    public fun startRendering(player: ServerPlayer)

    /**
     * Called when a player stops observing the boundary.
     */
    public fun stopRendering(player: ServerPlayer)

    /**
     * Called to re-send any initializing rendering state.
     *
     * This should essentially send any packets sent during [startRendering]
     * but should not modify any state of the renderer for the given [player].
     *
     * @param player The player to restart rendering for.
     * @param sender The packet sender.
     */
    public fun restartRendering(player: ServerPlayer, sender: Consumer<Packet<ClientGamePacketListener>>)

    /**
     * Creates a [Factory] for this renderer implementation.
     *
     * @return The renderer factory.
     */
    public fun factory(): Factory

    /**
     * A factory interface which creates [BoundaryRenderer]s.
     *
     * You should register your factory codecs to [BoundaryRegistries.BOUNDARY_RENDERER_FACTORY].
     */
    public interface Factory {
        /**
         * Creates an implementation of [BoundaryRenderer].
         *
         * @param shape The shape to render.
         * @return The renderer.
         */
        public fun create(shape: BoundaryShape): BoundaryRenderer

        /**
         * The codec for this factory.
         *
         * @return The codec.
         */
        public fun codec(): MapCodec<out Factory>

        public companion object {
            /**
             * Codec for registered [Factory]s.
             */
            public val CODEC: Codec<Factory> = Codec.lazyInitialized {
                BoundaryRegistries.BOUNDARY_RENDERER_FACTORY.byNameCodec()
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