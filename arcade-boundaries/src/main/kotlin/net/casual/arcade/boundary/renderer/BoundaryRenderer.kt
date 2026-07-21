/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.boundary.renderer

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import net.casual.arcade.boundary.shape.BoundaryShape
import net.casual.arcade.boundary.utils.BoundaryRegistries
import net.casual.arcade.utils.serialization.codec.CodecProvider.Companion.register
import net.minecraft.core.Registry
import net.minecraft.server.level.ServerLevel
import org.jetbrains.annotations.ApiStatus.OverrideOnly
import java.util.function.Function

/**
 * This provides an interface for rendering [BoundaryShape]s for a [ServerLevel].
 *
 * @see BoundaryShape
 */
public interface BoundaryRenderer {
    /**
     * Updates the renderer, this is called every tick.
     */
    public fun render()

    /**
     * Creates a [Factory] for this renderer implementation.
     *
     * @return The renderer factory.
     */
    public fun factory(): Factory

    /**
     * Called when the boundary is removed.
     * This should stop rendering for all observers.
     */
    @OverrideOnly
    public fun stop() {

    }

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
        public fun create(
            level: ServerLevel,
            shape: BoundaryShape
        ): BoundaryRenderer

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