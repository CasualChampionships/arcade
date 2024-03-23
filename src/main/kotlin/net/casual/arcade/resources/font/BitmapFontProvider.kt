package net.casual.arcade.resources.font

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.senseiwells.nametag.impl.serialization.ResourceLocationSerializer
import net.minecraft.resources.ResourceLocation

@Serializable
@SerialName("bitmap")
public data class BitmapFontProvider(
    @SerialName("file")
    @Serializable(with = ResourceLocationSerializer::class)
    val texture: ResourceLocation,
    val ascent: Int = 8,
    val height: Int = 8,
    val chars: List<String>
): FontProvider