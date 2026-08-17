/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.pack.font.providers

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.resources.Identifier

public data class BitmapFontProvider(
    val texture: Identifier,
    val ascent: Int = 8,
    val height: Int = 8,
    val chars: List<String>
): FontProvider {
    override val type: FontProviderType
        get() = FontProviderType.Bitmap

    public companion object {
        public val CODEC: MapCodec<BitmapFontProvider> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Identifier.CODEC.fieldOf("file").forGetter(BitmapFontProvider::texture),
                Codec.INT.fieldOf("ascent").forGetter(BitmapFontProvider::ascent),
                Codec.INT.fieldOf("height").forGetter(BitmapFontProvider::height),
                Codec.STRING.listOf().fieldOf("chars").forGetter(BitmapFontProvider::chars)
            ).apply(instance, ::BitmapFontProvider)
        }
    }
}