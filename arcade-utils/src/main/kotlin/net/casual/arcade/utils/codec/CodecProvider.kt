/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.codec

import com.mojang.serialization.MapCodec
import net.casual.arcade.utils.serialization.codec.CodecProvider
import net.minecraft.core.Registry

@Deprecated("Moved", ReplaceWith("net.casual.arcade.utils.serialization.codec.CodecProvider"))
public interface CodecProvider<T>: CodecProvider<T> {
    public companion object {
        public fun <T> CodecProvider<out T>.register(registry: Registry<MapCodec<out T>>) {
            Registry.register(registry, this.ID, this.CODEC)
        }
    }
}