/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.serialization.codec

import com.mojang.serialization.MapCodec
import net.minecraft.core.Registry
import net.minecraft.resources.Identifier

public interface CodecProvider<T> {
    public val id: Identifier

    public val codec: MapCodec<out T>

    public companion object {
        public fun <T> CodecProvider<out T>.register(registry: Registry<MapCodec<out T>>) {
            Registry.register(registry, this.id, this.codec)
        }
    }
}