/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.FieldEncoder
import net.casual.arcade.utils.codec.EncodedOptionalFieldCodec
import net.casual.arcade.utils.codec.FieldDecoderOfAny
import net.casual.arcade.utils.codec.OptionalCodec
import java.util.*
import kotlin.collections.LinkedHashSet
import kotlin.jvm.optionals.getOrNull

public fun <A: Any> Codec<A>.lenientOptionalOf(): Codec<Optional<A>> {
    return this.optionalOf(true)
}

public fun <A: Any> Codec<A>.optionalOf(
    lenient: Boolean = false
): Codec<Optional<A>> {
    return OptionalCodec(this, lenient)
}

public fun <A: Any> Codec<A>.encodedOptionalFieldOf(name: String): MapCodec<Optional<A>> {
    return EncodedOptionalFieldCodec(name, this)
}

public fun <A: Any> Codec<A>.encodedOptionalFieldOf(name: String, defaultValue: A): MapCodec<A> {
    return EncodedOptionalFieldCodec(name, this).xmap(
        { o -> o.orElse(defaultValue) },
        { a -> Optional.of(a) }
    )
}

public fun <A> Codec<A>.fieldOfAny(primary: String, vararg secondaries: String): MapCodec<A> {
    val encoder = FieldEncoder(primary, this)
    val decoder = FieldDecoderOfAny(primary, secondaries, this)
    return MapCodec.of(encoder, decoder)
}

public fun <A> Codec<A>.setOf(): Codec<Set<A>> {
    return this.listOf().xmap(::LinkedHashSet, ::ArrayList)
}