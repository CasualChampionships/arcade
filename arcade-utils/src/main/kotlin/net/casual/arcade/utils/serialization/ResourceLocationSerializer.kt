package net.casual.arcade.utils.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.ResourceLocationException
import net.minecraft.resources.ResourceLocation

public object ResourceLocationSerializer: KSerializer<ResourceLocation> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ArcadeResourceLocationSerializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ResourceLocation) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): ResourceLocation {
        val location = decoder.decodeString()
        try {
            return ResourceLocation.parse(location)
        } catch (e: ResourceLocationException) {
            throw SerializationException("Invalid ResourceLocation")
        }
    }
}