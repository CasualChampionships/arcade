package net.casual.arcade.resources.font.providers

import com.mojang.serialization.Codec

public sealed interface FontProvider {
    public val type: FontProviderType

    public companion object {
        public val CODEC: Codec<FontProvider> = FontProviderType.CODEC.dispatch(FontProvider::type, FontProviderType::codec)
    }
}