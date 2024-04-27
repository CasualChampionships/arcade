package net.casual.arcade.utils.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.FloatArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.world.phys.Vec2

@OptIn(ExperimentalSerializationApi::class)
public object Vec2Serializer: KSerializer<Vec2> {
    override val descriptor: SerialDescriptor = SerialDescriptor("Vec2", FloatArraySerializer().descriptor)

    override fun deserialize(decoder: Decoder): Vec2 {
        val vector = decoder.decodeSerializableValue(FloatArraySerializer())
        if (vector.size != 2) {
            throw SerializationException("Vec2 expected 2 floats")
        }
        return Vec2(vector[0], vector[1])
    }

    override fun serialize(encoder: Encoder, value: Vec2) {
        encoder.encodeSerializableValue(FloatArraySerializer(), floatArrayOf(value.x, value.y))
    }
}