/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.serialization.codec

import com.google.common.collect.HashBiMap
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.FieldEncoder
import java.util.*

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

public fun <A> Codec<A>.collectionOf(): Codec<Collection<A>> {
    return this.listOf().xmap({ it }, ::ArrayList)
}

public fun <A, K> Codec<List<A>>.associateBy(key: (A) -> K): Codec<Map<K, A>> {
    return this.xmap({ it.associateByTo(LinkedHashMap(), key) }, { it.values.toList() })
}

public fun <K, V> Codec<K>.map(map: HashBiMap<K, V>): Codec<V> {
    val inverse = map.inverse()
    return this.flatXmap(
        { key ->
            val value = map[key]
            if (value != null) DataResult.success(value) else DataResult.error { "Key '${key}' has no mapping" }
        },
        { value ->
            val key = inverse[value]
            if (key != null) DataResult.success(key) else DataResult.error { "Value '${value}' has no mapping" }
        }
    )
}