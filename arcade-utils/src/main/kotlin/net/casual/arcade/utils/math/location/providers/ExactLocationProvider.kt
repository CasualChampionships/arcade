/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.math.location.providers

import com.mojang.serialization.MapCodec
import net.casual.arcade.utils.arcade
import net.casual.arcade.utils.math.location.Location
import net.casual.arcade.utils.serialization.codec.CodecProvider
import net.minecraft.resources.Identifier

public class ExactLocationProvider(
    private val location: Location = Location.DEFAULT
): LocationProvider {
    override fun get(): Location {
        return this.location
    }

    override fun codec(): MapCodec<out LocationProvider> {
        return codec
    }

    public companion object: CodecProvider<ExactLocationProvider> {
        override val id: Identifier = arcade("exact")

        override val codec: MapCodec<out ExactLocationProvider> = Location.MAP_CODEC.xmap(
            { location -> ExactLocationProvider(location) },
            { provider -> provider.location }
        )
    }
}