/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.codec

import com.mojang.serialization.DataResult
import com.mojang.serialization.Decoder
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.MapDecoder
import com.mojang.serialization.MapLike
import java.util.stream.Stream

public class FieldDecoderOfAny<A>(
    primary: String,
    secondaries: Array<out String>,
    private val codec: Decoder<A>
): MapDecoder.Implementation<A>() {
    private val names = listOf(primary, *secondaries)

    override fun <T: Any> keys(ops: DynamicOps<T>): Stream<T> {
        return this.names.stream().map(ops::createString)
    }

    override fun <T: Any> decode(ops: DynamicOps<T>, input: MapLike<T>): DataResult<A> {
        for (name in this.names) {
            val value = input.get(name) ?: continue
            return this.codec.parse(ops, value)
        }
        return DataResult.error { "No any key matching ${this.names} in $input" }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is FieldDecoderOfAny<*>) {
            return false
        }
        return this.codec == other.codec && this.names == other.names
    }

    override fun hashCode(): Int {
        var result = this.codec.hashCode()
        result = 31 * result + this.names.hashCode()
        return result
    }

    override fun toString(): String {
        return "FieldDecoderOfAny[${this.names}: ${this.codec}]"
    }
}