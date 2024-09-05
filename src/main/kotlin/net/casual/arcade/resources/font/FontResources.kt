package net.casual.arcade.resources.font

import com.google.common.collect.HashMultimap
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import net.casual.arcade.resources.lang.LanguageEntry
import net.casual.arcade.utils.ComponentUtils
import net.casual.arcade.utils.ComponentUtils.withFont
import net.minecraft.resources.ResourceLocation
import org.apache.commons.lang3.mutable.MutableInt
import com.google.gson.JsonObject as GsonObject

public abstract class FontResources(
    public val id: ResourceLocation
) {
    private val languages = HashMultimap.create<String, LanguageEntry>()
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

    protected fun translatable(
        key: String,
        translations: Translatable.() -> Unit
    ): ComponentUtils.ConstantComponentGenerator {
        Translatable(this, key).translations()
        return ComponentUtils.translatable(key) {
            withFont(id)
        }
    }

    protected fun at(path: String): ResourceLocation {
        return ResourceLocation.fromNamespaceAndPath(this.id.namespace, "font/$path")
    }

    internal fun toJson(): String {
        val font = buildJsonObject {
            put("providers", json.encodeToJsonElement(providers))
        }
        return json.encodeToString(font)
    }

    internal fun getLangJsons(): Map<String, GsonObject> {
        val langs = HashMap<String, GsonObject>()
        for (lang in this.languages.keySet()) {
            val translations = GsonObject()
            for (entry in this.languages.get(lang)) {
                translations.addProperty(entry.key, entry.translation)
            }
            langs[lang] = translations
        }
        return langs
    }

    private fun nextBitmapChar(): Char {
        return bitmapIndex.andIncrement.toChar()
    }

    protected class Translatable(
        private val resources: FontResources,
        private val key: String
    ) {
        public fun bitmap(
            lang: String,
            texture: ResourceLocation,
            ascent: Int = 8,
            height: Int = 8
        ) {
            val key = this.resources.nextBitmapChar().toString()
            val bitmap = BitmapFontProvider(texture, ascent, height, listOf(key))
            this.resources.providers.add(bitmap)
            this.resources.languages.put(lang, LanguageEntry(this.key, key))
        }
    }

    private companion object {
        val json = Json {
            encodeDefaults = true
        }
    }
}