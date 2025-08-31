/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.serialization.kotlin

import com.mojang.serialization.Codec
import com.mojang.serialization.DynamicOps
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder

public class KJsonCodecSerializer<T>(
    private val codec: Codec<T>,
    private val ops: DynamicOps<JsonElement>
): KSerializer<T> {
    override val descriptor: SerialDescriptor = JsonElement.serializer().descriptor

    override fun deserialize(decoder: Decoder): T {
        if (decoder !is JsonDecoder) {
            throw SerializationException("Cannot deserialize, decoder is not a JSON decoder")
        }
        return this.codec.parse(this.ops, decoder.decodeJsonElement())
            .getPartialOrThrow(::SerializationException)
    }

    override fun serialize(encoder: Encoder, value: T) {
        if (encoder !is JsonEncoder) {
            throw SerializationException("Cannot serialize $value, encoder is not a JSON encoder")
        }
        this.codec.encodeStart(this.ops, value)
    }
}