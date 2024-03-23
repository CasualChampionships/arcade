package net.casual.arcade.resources.font

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import net.casual.arcade.utils.ComponentUtils
import net.casual.arcade.utils.ComponentUtils.withFont
import net.minecraft.resources.ResourceLocation
import org.apache.commons.lang3.mutable.MutableInt
import java.io.ByteArrayOutputStream

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
            put("providers", json.encodeToJsonElement(providers))
        }
        return json.encodeToString(font)
    }

    private fun nextBitmapChar(): Char {
        return bitmapIndex.andIncrement.toChar()
    }

    private companion object {
        val json = Json {
            encodeDefaults = true
        }
    }
}