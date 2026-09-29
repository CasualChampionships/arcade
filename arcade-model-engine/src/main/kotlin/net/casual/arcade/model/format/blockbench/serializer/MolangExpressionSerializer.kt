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
import net.casual.arcade.model.animation.molang.MolangExpression

public typealias SerializableMolangExpression = @Serializable(with = MolangExpressionSerializer::class) MolangExpression

public object MolangExpressionSerializer: KSerializer<MolangExpression> {
    override val descriptor: SerialDescriptor = serializer<String>().descriptor

    override fun serialize(encoder: Encoder, value: MolangExpression) {
        encoder.encodeString(value.source)
    }

    override fun deserialize(decoder: Decoder): MolangExpression {
        val content = JsonPrimitiveContentSerializer.deserialize(decoder) ?: return MolangExpression.ZERO
        return MolangExpression.parse(content)
    }
}