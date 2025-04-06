/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.math.location.providers

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.utils.ResourceUtils
import net.casual.arcade.utils.codec.CodecProvider
import net.casual.arcade.utils.math.location.Location
import net.minecraft.resources.ResourceLocation

public class CyclingLocationProvider(
    private val locations: List<LocationProvider>
): LocationProvider {
    private var index = 0

    override fun get(): Location {
        return this.locations[this.index++ % this.locations.size].get()
    }

    override fun get(count: Int): List<Location> {
        val list = ArrayList<Location>(count)
        for (i in 0 until count) {
            list.add(this.locations[i % this.locations.size].get())
        }
        return list
    }

    override fun get(origin: Location): Location {
        return this.locations[this.index++ % this.locations.size].get(origin)
    }

    override fun get(origin: Location, count: Int): List<Location> {
        val list = ArrayList<Location>(count)
        for (i in 0 until count) {
            list.add(this.locations[i % this.locations.size].get(origin))
        }
        return list
    }

    override fun codec(): MapCodec<out LocationProvider> {
        return CODEC
    }

    public companion object: CodecProvider<CyclingLocationProvider> {
        override val ID: ResourceLocation = ResourceUtils.arcade("cycling")

        override val CODEC: MapCodec<out CyclingLocationProvider> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                LocationProvider.CODEC.listOf(1, Int.MAX_VALUE).fieldOf("locations").forGetter(CyclingLocationProvider::locations)
            ).apply(instance, ::CyclingLocationProvider)
        }
    }
}