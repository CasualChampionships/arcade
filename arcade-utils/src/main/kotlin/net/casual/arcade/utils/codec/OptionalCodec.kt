/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.codec

import com.mojang.datafixers.util.Pair
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import java.util.Optional

public class OptionalCodec<A: Any>(
    private val codec: Codec<A>,
    private val lenient: Boolean
): Codec<Optional<A>> {
    override fun <T: Any> encode(input: Optional<A>, ops: DynamicOps<T>, prefix: T): DataResult<T> {
        if (input.isEmpty) {
            return ops.mergeToPrimitive(prefix, ops.empty())
        }
        return this.codec.encode(input.get(), ops, prefix)
    }

    override fun <T: Any> decode(ops: DynamicOps<T>, input: T): DataResult<Pair<Optional<A>, T>> {
        if (input == ops.empty()) {
            return DataResult.success(Pair(Optional.empty(), input))
        }
        val result = this.codec.decode(ops, input)
        if (result.isError && this.lenient) {
            return DataResult.success(Pair(Optional.empty(), input))
        }
        return result.map { pair -> Pair(Optional.ofNullable(pair.first), pair.second) }
    }
}