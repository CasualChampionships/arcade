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

    public fun get(count: Int): List<Location> {
        val list = ArrayList<Location>(count)
        for (i in 0 until count) {
            list.add(this.get())
        }
        return list
    }

    public fun get(origin: Location): Location {
        return this.get()
    }

    public fun get(origin: Location, count: Int): List<Location> {
        val list = ArrayList<Location>(count)
        for (i in 0 until count) {
            list.add(this.get(origin))
        }
        return list
    }

    public fun codec(): MapCodec<out LocationProvider>

    public companion object {
        public val DEFAULT: LocationProvider = ExactLocationProvider()

        public val CODEC: Codec<LocationProvider> = Codec.lazyInitialized {
            ArcadeUtilsRegistries.LOCATION_PROVIDER.byNameCodec()
                .dispatch(LocationProvider::codec, Function.identity())
        }

        internal fun bootstrap(registry: Registry<MapCodec<out LocationProvider>>) {
            AroundLocationProvider.register(registry)
            BlendedLocationProvider.register(registry)
            CyclingLocationProvider.register(registry)
            ExactLocationProvider.register(registry)
            LocalLocationProvider.register(registry)
            RandomLocationProvider.register(registry)
            RelativeLocationProvider.register(registry)
            WithOriginLocationProvider.register(registry)
        }
    }
}