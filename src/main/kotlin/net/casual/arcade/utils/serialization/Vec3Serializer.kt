package net.casual.arcade.utils.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.DoubleArraySerializer
import kotlinx.serialization.builtins.FloatArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3

@OptIn(ExperimentalSerializationApi::class)
public object Vec3Serializer: KSerializer<Vec3> {
    override val descriptor: SerialDescriptor = SerialDescriptor("Vec3", DoubleArraySerializer().descriptor)

    override fun deserialize(decoder: Decoder): Vec3 {
        val vector = decoder.decodeSerializableValue(DoubleArraySerializer())
        if (vector.size != 3) {
            throw SerializationException("Vec3 expected 3 doubles")
        }
        return Vec3(vector[0], vector[1], vector[2])
    }

    override fun serialize(encoder: Encoder, value: Vec3) {
        encoder.encodeSerializableValue(DoubleArraySerializer(), doubleArrayOf(value.x, value.y, value.z))
    }
}