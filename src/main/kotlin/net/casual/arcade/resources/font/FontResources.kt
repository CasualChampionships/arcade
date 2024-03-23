package net.casual.arcade.resources.font

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import net.casual.arcade.utils.ComponentUtils
import net.casual.arcade.utils.ComponentUtils.withFont
import net.minecraft.resources.ResourceLocation
import org.apache.commons.lang3.mutable.MutableInt

public abstract class FontResources(
    public val id: ResourceLocation
) {
    private val bitmapIndex = MutableInt(0xE000)
    private val providers = ArrayList<FontProvider>()

    protected fun bitmap(
        texture: ResourceLocation,
        ascent: Int = 8,
        height: Int = 8
    ): ComponentUtils.ConstantComponentGenerator {
        val key = this.nextBitmapChar().toString()
        val bitmap = BitmapFontProvider(texture, ascent, height, listOf(key))
        this.providers.add(bitmap)
        return ComponentUtils.literal(key) {
            withFont(id)
        }
    }

    protected fun at(path: String): ResourceLocation {
        return ResourceLocation(this.id.namespace, "font/$path")
    }

    internal fun getJson(): String {
        val font = buildJsonObject {
            put("providers", Json.encodeToJsonElement(providers))
        }
        return Json.encodeToString(font)
    }

    private fun nextBitmapChar(): Char {
        return bitmapIndex.andIncrement.toChar()
    }
}