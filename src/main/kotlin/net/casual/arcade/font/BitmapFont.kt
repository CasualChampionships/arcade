package net.casual.arcade.font

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.casual.arcade.utils.ComponentUtils
import net.casual.arcade.utils.ComponentUtils.withFont
import net.minecraft.resources.ResourceLocation
import org.apache.commons.lang3.mutable.MutableInt

public abstract class BitmapFont(
    public val id: ResourceLocation
) {
    private val bitmapIndex = MutableInt(0xE000)
    private val providers = ArrayList<BitmapFontProvider>()

    protected fun add(
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

    internal fun getData(): ByteArray {
        val font = JsonObject()
        val providers = JsonArray()
        for (provider in this.providers) {
            providers.add(provider.serialize())
        }
        font.add("providers", providers)
        return Gson().toJson(font).encodeToByteArray()
    }

    private fun nextBitmapChar(): Char {
        return bitmapIndex.andIncrement.toChar()
    }
}