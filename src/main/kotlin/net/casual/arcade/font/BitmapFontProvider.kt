package net.casual.arcade.font

import com.google.gson.JsonObject
import net.casual.arcade.utils.JsonUtils.set
import net.casual.arcade.utils.JsonUtils.toJsonStringArray
import net.minecraft.resources.ResourceLocation

public class BitmapFontProvider(
    private val texture: ResourceLocation,
    private val ascent: Int = 8,
    private val height: Int = 8,
    private val chars: List<String>
) {
    public fun serialize(): JsonObject {
        val json = JsonObject()
        json["type"] = "bitmap"
        json["file"] = this.texture.toString()
        json["ascent"] = this.ascent
        json["height"] = this.height
        json["chars"] = this.chars.toJsonStringArray { it }
        return json
    }
}