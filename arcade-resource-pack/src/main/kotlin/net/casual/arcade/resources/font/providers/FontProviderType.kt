/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.resources.font.providers

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import net.minecraft.util.StringRepresentable

public enum class FontProviderType(
    public val id: String,
    public val codec: MapCodec<out FontProvider>
): StringRepresentable {
    Bitmap("bitmap", BitmapFontProvider.CODEC),
    Space("space", SpaceFontProvider.CODEC);

    override fun getSerializedName(): String {
        return this.id
    }

    public companion object {
        public val CODEC: Codec<FontProviderType> = StringRepresentable.fromEnum(FontProviderType::values)
    }
}