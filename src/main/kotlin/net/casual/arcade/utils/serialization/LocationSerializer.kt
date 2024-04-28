package net.casual.arcade.utils.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.serializer
import net.casual.arcade.minigame.events.lobby.LocationConfig

public object LocationSerializer: KSerializer<LocationConfig> {
    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor: SerialDescriptor = SerialDescriptor("Location", serializer<Map<String, Number>>().descriptor)

    override fun deserialize(decoder: Decoder): LocationConfig {
        val map = decoder.decodeSerializableValue(serializer<Map<String, Number>>())
        return LocationConfig(
            map.getOrDefault("x", 0.0).toDouble(),
            map.getOrDefault("y", 0.0).toDouble(),
            map.getOrDefault("z", 0.0).toDouble(),
            map.getOrDefault("yaw", 0.0).toFloat(),
            map.getOrDefault("pitch", 0.0).toFloat()
        )
    }

    override fun serialize(encoder: Encoder, value: LocationConfig) {
        val map = mapOf(
            "x" to value.x,
            "y" to value.y,
            "z" to value.z,
            "yaw" to value.yaw,
            "pitch" to value.pitch
        )
        encoder.encodeSerializableValue(serializer<Map<String, Number>>(), map)
    }
}