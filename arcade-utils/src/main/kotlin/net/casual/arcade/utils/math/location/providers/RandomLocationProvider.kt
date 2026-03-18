/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.math.location.providers

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.utils.IdentifierUtils
import net.casual.arcade.utils.math.location.Location
import net.casual.arcade.utils.serialization.codec.CodecProvider
import net.minecraft.resources.Identifier

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
        return codec
    }

    public companion object: CodecProvider<RandomLocationProvider> {
        override val id: Identifier = IdentifierUtils.arcade("random")

        override val codec: MapCodec<out RandomLocationProvider> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                LocationProvider.CODEC.listOf(1, Int.MAX_VALUE).fieldOf("locations").forGetter(RandomLocationProvider::locations)
            ).apply(instance, ::RandomLocationProvider)
        }
    }
}