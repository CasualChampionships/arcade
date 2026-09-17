/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.model.format.blockbench.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.serializer

internal typealias LenientFloat = @Serializable(with = JsonLenientFloatSerializer::class) Float

internal object JsonLenientFloatSerializer: KSerializer<Float> {
    override val descriptor: SerialDescriptor = serializer<Float>().descriptor

    override fun serialize(encoder: Encoder, value: Float) {
        encoder.encodeFloat(value)
    }

    override fun deserialize(decoder: Decoder): Float {
        return JsonPrimitiveContentSerializer.deserialize(decoder)?.toFloatOrNull() ?: 0.0F
    }
}