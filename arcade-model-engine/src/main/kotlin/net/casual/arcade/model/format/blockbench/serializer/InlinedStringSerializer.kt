/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.model.format.blockbench.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.serializer

internal open class InlinedStringSerializer<T>(
    private val getter: (T) -> String,
    private val constructor: (String) -> T
): KSerializer<T> {
    override val descriptor: SerialDescriptor = serializer<String>().descriptor

    override fun serialize(encoder: Encoder, value: T) {
        encoder.encodeString(this.getter.invoke(value))
    }

    override fun deserialize(decoder: Decoder): T {
        return this.constructor.invoke(decoder.decodeString())
    }
}