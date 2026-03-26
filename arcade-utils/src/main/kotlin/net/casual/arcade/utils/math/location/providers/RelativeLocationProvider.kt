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

public class RelativeLocationProvider(
    private val offset: Location
): LocationProvider {
    override fun get(): Location {
        return this.offset
    }

    override fun get(origin: Location): Location {
        return Location(
            origin.position.add(this.offset.position),
            origin.rotation.add(this.offset.rotation)
        )
    }

    override fun codec(): MapCodec<out LocationProvider> {
        return codec
    }

    public companion object: CodecProvider<RelativeLocationProvider> {
        override val id: Identifier = arcade("relative")

        override val codec: MapCodec<out RelativeLocationProvider> = Location.MAP_CODEC.xmap(::RelativeLocationProvider, RelativeLocationProvider::offset)
    }
}