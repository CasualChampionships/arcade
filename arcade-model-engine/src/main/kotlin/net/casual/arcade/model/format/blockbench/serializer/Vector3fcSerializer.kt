/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.model.format.blockbench.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.FloatArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.joml.Vector3f
import org.joml.Vector3fc

public typealias SerializableVector3fc = @Serializable(with = Vector3fcSerializer::class) Vector3fc

internal object Vector3fcSerializer: KSerializer<Vector3fc> {
    private val delegate = FloatArraySerializer()

    override val descriptor: SerialDescriptor = this.delegate.descriptor

    override fun serialize(encoder: Encoder, value: Vector3fc) {
        this.delegate.serialize(encoder, floatArrayOf(value.x(), value.y(), value.z()))
    }

    override fun deserialize(decoder: Decoder): Vector3fc {
        val array = this.delegate.deserialize(decoder)
        return if (array.size < 3) Vector3f() else Vector3f(array[0], array[1], array[2])
    }
}