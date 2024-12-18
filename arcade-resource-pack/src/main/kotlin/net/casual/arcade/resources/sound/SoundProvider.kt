/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
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
            return when (this) {
                Sound -> "file"
                Event -> "event"
            }
        }

        public companion object {
            public val CODEC: Codec<Type> = StringRepresentable.fromEnum(Type::values)
        }
    }

    public companion object {
        public val CODEC: Codec<SoundProvider> = RecordCodecBuilder.create { instance ->
            instance.group(
                ResourceLocation.CODEC.fieldOf("name").forGetter(SoundProvider::id),
                Codec.FLOAT.optionalFieldOf("volume", 1.0F).forGetter(SoundProvider::volume),
                Codec.FLOAT.optionalFieldOf("pitch", 1.0F).forGetter(SoundProvider::pitch),
                Codec.INT.optionalFieldOf("weight", 1).forGetter(SoundProvider::weight),
                Codec.BOOL.optionalFieldOf("stream", false).forGetter(SoundProvider::stream),
                Codec.INT.optionalFieldOf("attenuation_distance", 1).forGetter(SoundProvider::attenuationDistance),
                Codec.BOOL.optionalFieldOf("preload", false).forGetter(SoundProvider::preload),
                Type.CODEC.optionalFieldOf("type", Type.Sound).forGetter(SoundProvider::type)
            ).apply(instance, ::SoundProvider)
        }
    }
}