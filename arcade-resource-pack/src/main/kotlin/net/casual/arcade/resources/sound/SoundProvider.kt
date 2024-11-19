package net.casual.arcade.resources.sound

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.StringRepresentable

public data class SoundProvider(
    val id: ResourceLocation,
    val volume: Float = 1.0F,
    val pitch: Float = 1.0F,
    val weight: Int = 1,
    val stream: Boolean = false,
    val attenuationDistance: Int = 16,
    val preload: Boolean = false,
    val type: Type = Type.Sound
) {
    public enum class Type: StringRepresentable {
        Sound,
        Event;

        override fun getSerializedName(): String {
            return this.name.lowercase()
        }

        public companion object {
            public val CODEC: Codec<Type> = StringRepresentable.fromEnum(Type::values)
        }
    }

    public companion object {
        public val CODEC: Codec<SoundProvider> = RecordCodecBuilder.create { instance ->
            instance.group(
                ResourceLocation.CODEC.fieldOf("name").forGetter(SoundProvider::id),
                Codec.FLOAT.fieldOf("volume").forGetter(SoundProvider::volume),
                Codec.FLOAT.fieldOf("pitch").forGetter(SoundProvider::pitch),
                Codec.INT.fieldOf("weight").forGetter(SoundProvider::weight),
                Codec.BOOL.fieldOf("stream").forGetter(SoundProvider::stream),
                Codec.INT.fieldOf("attenuation_distance").forGetter(SoundProvider::attenuationDistance),
                Codec.BOOL.fieldOf("preload").forGetter(SoundProvider::preload),
                Type.CODEC.fieldOf("type").forGetter(SoundProvider::type)
            ).apply(instance, ::SoundProvider)
        }
    }
}