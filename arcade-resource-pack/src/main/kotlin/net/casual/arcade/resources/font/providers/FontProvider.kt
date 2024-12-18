/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.resources.font.providers

import com.mojang.serialization.Codec

public sealed interface FontProvider {
    public val type: FontProviderType

    public companion object {
        public val CODEC: Codec<FontProvider> = FontProviderType.CODEC.dispatch(FontProvider::type, FontProviderType::codec)
    }
}