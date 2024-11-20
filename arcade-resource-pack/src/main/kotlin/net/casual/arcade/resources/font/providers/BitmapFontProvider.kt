package net.casual.arcade.resources.font.providers

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.resources.ResourceLocation

public data class BitmapFontProvider(
    val texture: ResourceLocation,
    val ascent: Int = 8,
    val height: Int = 8,
    val chars: List<String>
): FontProvider {
    override val type: FontProviderType
        get() = FontProviderType.Bitmap

    public companion object {
        public val CODEC: MapCodec<BitmapFontProvider> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                ResourceLocation.CODEC.fieldOf("file").forGetter(BitmapFontProvider::texture),
                Codec.INT.fieldOf("ascent").forGetter(BitmapFontProvider::ascent),
                Codec.INT.fieldOf("height").forGetter(BitmapFontProvider::height),
                Codec.STRING.listOf().fieldOf("chars").forGetter(BitmapFontProvider::chars)
            ).apply(instance, ::BitmapFontProvider)
        }
    }
}