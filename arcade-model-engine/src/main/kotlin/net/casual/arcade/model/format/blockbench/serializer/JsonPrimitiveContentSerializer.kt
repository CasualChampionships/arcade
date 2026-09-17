/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.model.format.blockbench.serializer

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.serializer

internal typealias LenientTextureId = @Serializable(with = JsonPrimitiveContentSerializer::class) String?
internal typealias RawMolangExpression = @Serializable(with = JsonPrimitiveContentSerializer::class) String?

@OptIn(ExperimentalSerializationApi::class)
internal object JsonPrimitiveContentSerializer: KSerializer<String?> {
    override val descriptor: SerialDescriptor = serializer<String>().descriptor

    override fun serialize(encoder: Encoder, value: String?) {
        if (value == null) encoder.encodeNull() else encoder.encodeString(value)
    }

    override fun deserialize(decoder: Decoder): String? {
        require(decoder is JsonDecoder)
        return decoder.decodeJsonElement().jsonPrimitive.contentOrNull
    }
}