/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.math.location.providers

import com.mojang.serialization.MapCodec
import net.casual.arcade.utils.ResourceUtils
import net.casual.arcade.utils.codec.CodecProvider
import net.casual.arcade.utils.math.location.Location
import net.minecraft.resources.ResourceLocation

public class ExactLocationProvider(
    private val location: Location = Location.DEFAULT
): LocationProvider {
    override fun get(): Location {
        return this.location
    }

    override fun codec(): MapCodec<out LocationProvider> {
        return CODEC
    }

    public companion object: CodecProvider<ExactLocationProvider> {
        override val ID: ResourceLocation = ResourceUtils.arcade("exact")

        override val CODEC: MapCodec<out ExactLocationProvider> = Location.MAP_CODEC.xmap(
            { location -> ExactLocationProvider(location) },
            { provider -> provider.location }
        )
    }
}