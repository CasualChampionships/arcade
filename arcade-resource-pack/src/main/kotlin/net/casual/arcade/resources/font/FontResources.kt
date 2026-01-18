/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.resources.font

import com.google.common.collect.HashMultimap
import com.google.gson.JsonObject
import com.mojang.serialization.JsonOps
import it.unimi.dsi.fastutil.ints.Int2FloatLinkedOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.casual.arcade.resources.font.providers.BitmapFontProvider
import net.casual.arcade.resources.font.providers.FontProvider
import net.casual.arcade.resources.font.providers.SpaceFontProvider
import net.casual.arcade.resources.lang.LanguageEntry
import net.casual.arcade.utils.component.font
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import org.apache.commons.lang3.mutable.MutableInt
import java.awt.image.BufferedImage

public abstract class FontResources(
    public val id: ResourceLocation,
    pua: FontPUA = FontPUA.Plane0
) {
    private val languages = HashMultimap.create<String, LanguageEntry>()
    private val codepoint = MutableInt(pua.codepoint)
    private val providers = ArrayList<FontProvider>()
    private val bitmaps = Object2ObjectOpenHashMap<ResourceLocation, BitmapGenerator>()
    private val spaces by lazy(::createSpaces)

    protected fun space(advance: Float): Component {
        val codepoint = this.nextCodepoint()
        this.spaces[codepoint] = advance
        return Component.literal(Character.toString(codepoint)).font(id)
    }

    protected fun bitmap(
        texture: ResourceLocation,
        ascent: Int = 8,
        height: Int = 8,
        generator: BitmapGenerator? = null
    ): Component {
        val codepoint = this.nextCodepointAsString()
        val bitmap = BitmapFontProvider(texture.addPNGSuffix(), ascent, height, listOf(codepoint))
        this.providers.add(bitmap)
        if (generator != null) {
            this.bitmaps[texture.removePNGSuffix()] = generator
        }
        return Component.literal(codepoint).font(id)
    }

    protected fun translatable(
        key: String,
        translations: Translatable.() -> Unit
    ): Component {
        Translatable(this, key).translations()
        return Component.translatable(key).font(id)
    }

    protected fun at(path: String): ResourceLocation {
        return ResourceLocation.fromNamespaceAndPath(this.id.namespace, "font/$path")
    }

    internal fun getProvidersJson(): JsonObject {
        val json = JsonObject()
        val result = FontProvider.CODEC.listOf().encodeStart(JsonOps.INSTANCE, this.providers).orThrow
        json.add("providers", result)
        return json
    }

    internal fun getLangJsons(): Map<String, JsonObject> {
        val langs = Object2ObjectOpenHashMap<String, JsonObject>()
        for (lang in this.languages.keySet()) {
            val translations = JsonObject()
            for (entry in this.languages.get(lang)) {
                translations.addProperty(entry.key, entry.translation)
            }
            langs[lang] = translations
        }
        return langs
    }

    internal fun getGeneratedBitmaps(): Map<ResourceLocation, BufferedImage> {
        val bitmaps = Object2ObjectOpenHashMap<ResourceLocation, BufferedImage>()
        for ((id, generator) in this.bitmaps) {
            bitmaps[id] = generator.generate(id)
        }
        return bitmaps
    }

    private fun ResourceLocation.addPNGSuffix(): ResourceLocation {
        if (!this.path.endsWith(".png")) {
            return this.withSuffix(".png")
        }
        return this
    }

    private fun ResourceLocation.removePNGSuffix(): ResourceLocation {
        if (this.path.endsWith(".png")) {
            return this.withPath { path -> path.removeSuffix(".png") }
        }
        return this
    }

    private fun nextCodepoint(): Int {
        return this.codepoint.andIncrement
    }

    private fun nextCodepointAsString(): String {
        return Character.toString(this.nextCodepoint())
    }

    private fun createSpaces(): Int2FloatLinkedOpenHashMap {
        val spaces = Int2FloatLinkedOpenHashMap()
        this.providers.add(SpaceFontProvider(spaces))
        return spaces
    }

    protected fun interface BitmapGenerator {
        public fun generate(id: ResourceLocation): BufferedImage
    }

    protected class Translatable(
        private val resources: FontResources,
        private val key: String
    ) {
        public fun space(lang: String, advance: Float) {
            val codepoint = this.resources.nextCodepoint()
            this.resources.spaces[codepoint] = advance
            this.resources.languages.put(lang, LanguageEntry(this.key, Character.toString(codepoint)))
        }

        public fun bitmap(
            lang: String,
            texture: ResourceLocation,
            ascent: Int = 8,
            height: Int = 8
        ) {
            val codepoint = this.resources.nextCodepointAsString()
            val bitmap = BitmapFontProvider(texture, ascent, height, listOf(codepoint))
            this.resources.providers.add(bitmap)
            this.resources.languages.put(lang, LanguageEntry(this.key, codepoint))
        }
    }
}