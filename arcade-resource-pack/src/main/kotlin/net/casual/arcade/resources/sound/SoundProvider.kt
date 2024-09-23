package net.casual.arcade.resources.sound

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.casual.arcade.utils.serialization.ResourceLocationSerializer
import net.minecraft.resources.ResourceLocation

@Serializable
public data class SoundProvider(
    @SerialName("name")
    @Serializable(with = ResourceLocationSerializer::class)
    val id: ResourceLocation,
    val volume: Float = 1.0F,
    val pitch: Float = 1.0F,
    val weight: Int = 1,
    val stream: Boolean = false,
    @SerialName("attenuation_distance")
    val attenuationDistance: Int = 16,
    val preload: Boolean = false,
    val type: Type = Type.Sound
) {
    @Serializable
    public enum class Type {
        @SerialName("sound")
        Sound,
        @SerialName("event")
        Event;
    }
}