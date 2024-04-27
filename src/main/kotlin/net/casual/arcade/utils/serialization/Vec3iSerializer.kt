package net.casual.arcade.utils.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.DoubleArraySerializer
import kotlinx.serialization.builtins.FloatArraySerializer
import kotlinx.serialization.builtins.IntArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.core.Vec3i
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3

@OptIn(ExperimentalSerializationApi::class)
public object Vec3iSerializer: KSerializer<Vec3i> {
    override val descriptor: SerialDescriptor = SerialDescriptor("Vec3i", IntArraySerializer().descriptor)

    override fun deserialize(decoder: Decoder): Vec3i {
        val vector = decoder.decodeSerializableValue(IntArraySerializer())
        if (vector.size != 3) {
            throw SerializationException("Vec3i expected 3 ints")
        }
        return Vec3i(vector[0], vector[1], vector[2])
    }

    override fun serialize(encoder: Encoder, value: Vec3i) {
        encoder.encodeSerializableValue(IntArraySerializer(), intArrayOf(value.x, value.y, value.z))
    }
}