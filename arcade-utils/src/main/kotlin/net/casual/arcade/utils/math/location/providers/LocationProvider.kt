/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.math.location.providers

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import net.casual.arcade.utils.codec.CodecProvider.Companion.register
import net.casual.arcade.utils.math.location.Location
import net.casual.arcade.utils.registries.ArcadeUtilsRegistries
import net.minecraft.core.Registry
import java.util.function.Function

public interface LocationProvider {
    public fun get(): Location

    public fun codec(): MapCodec<out LocationProvider>

    public companion object {
        public val DEFAULT: LocationProvider = ExactLocationProvider()

        public val CODEC: Codec<LocationProvider> = Codec.lazyInitialized {
            ArcadeUtilsRegistries.LOCATION_PROVIDER.byNameCodec()
                .dispatch(LocationProvider::codec, Function.identity())
        }

        internal fun bootstrap(registry: Registry<MapCodec<out LocationProvider>>) {
            ExactLocationProvider.register(registry)
            RandomLocationProvider.register(registry)
            AroundLocationProvider.register(registry)
        }
    }
}