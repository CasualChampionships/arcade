package net.casual.arcade.resources.font.providers

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.util.ExtraCodecs

public class SpaceFontProvider(
    private val advances: Map<Int, Float>
): FontProvider {
    override val type: FontProviderType
        get() = FontProviderType.Space

    public companion object {
        public val CODEC: MapCodec<SpaceFontProvider> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.unboundedMap(ExtraCodecs.CODEPOINT, Codec.FLOAT).fieldOf("advances").forGetter(SpaceFontProvider::advances)
            ).apply(instance, ::SpaceFontProvider)
        }
    }
}