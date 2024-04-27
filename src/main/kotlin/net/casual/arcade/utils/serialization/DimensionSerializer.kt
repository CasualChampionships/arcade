package net.casual.arcade.utils.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.ResourceLocationException
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.Level

public object DimensionSerializer: KSerializer<ResourceKey<Level>> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("dimension", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): ResourceKey<Level> {
        val location = decoder.decodeString()
        try {
            return ResourceKey.create(Registries.DIMENSION, ResourceLocation(location))
        } catch (e: ResourceLocationException) {
            throw SerializationException("Invalid dimension")
        }
    }

    override fun serialize(encoder: Encoder, value: ResourceKey<Level>) {
        encoder.encodeString(value.location().toString())
    }
}