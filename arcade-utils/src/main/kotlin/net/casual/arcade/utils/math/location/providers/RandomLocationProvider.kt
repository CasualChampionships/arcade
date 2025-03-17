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

public class RandomLocationProvider(
    private val locations: List<LocationProvider>
): LocationProvider {
    override fun get(): Location {
        return this.locations.random().get()
    }

    override fun get(origin: Location): Location {
        return this.locations.random().get(origin)
    }

    override fun codec(): MapCodec<out LocationProvider> {
        return CODEC
    }

    public companion object: CodecProvider<RandomLocationProvider> {
        override val ID: ResourceLocation = ResourceUtils.arcade("random")

        override val CODEC: MapCodec<out RandomLocationProvider> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                LocationProvider.CODEC.listOf(1, Int.MAX_VALUE).fieldOf("locations").forGetter(RandomLocationProvider::locations)
            ).apply(instance, ::RandomLocationProvider)
        }
    }
}