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

public class WithOriginLocationProvider(
    private val origin: LocationProvider,
    private val location: LocationProvider
): LocationProvider {
    override fun get(): Location {
        return this.location.get(this.origin.get())
    }

    override fun get(count: Int): List<Location> {
        return this.location.get(this.origin.get(), count)
    }

    override fun get(origin: Location): Location {
        return this.location.get(this.origin.get(origin))
    }

    override fun get(origin: Location, count: Int): List<Location> {
        return this.location.get(this.origin.get(origin), count)
    }

    override fun codec(): MapCodec<out LocationProvider> {
        return codec
    }

    public companion object: CodecProvider<WithOriginLocationProvider> {
        override val id: Identifier = IdentifierUtils.arcade("with_origin")

        override val codec: MapCodec<out WithOriginLocationProvider> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                LocationProvider.CODEC.fieldOf("origin").forGetter(WithOriginLocationProvider::origin),
                LocationProvider.CODEC.fieldOf("location").forGetter(WithOriginLocationProvider::location)
            ).apply(instance, ::WithOriginLocationProvider)
        }
    }
}