/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import net.casual.arcade.utils.codec.EncodedOptionalFieldCodec
import java.util.*
import kotlin.collections.LinkedHashSet

public fun <A: Any> Codec<A>.encodedOptionalFieldOf(name: String): MapCodec<Optional<A>> {
    return EncodedOptionalFieldCodec(name, this)
}

public fun <A: Any> Codec<A>.encodedOptionalFieldOf(name: String, defaultValue: A): MapCodec<A> {
    return EncodedOptionalFieldCodec(name, this).xmap(
        { o -> o.orElse(defaultValue) },
        { a -> Optional.of(a) }
    )
}

public fun <A: Any> Codec<A>.setOf(): Codec<Set<A>> {
    return this.listOf().xmap(::LinkedHashSet, ::ArrayList)
}