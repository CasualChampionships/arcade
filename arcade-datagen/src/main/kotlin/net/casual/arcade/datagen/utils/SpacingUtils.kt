package net.casual.arcade.datagen.utils

import net.casual.arcade.resources.font.spacing.SpacingFontResources
import net.casual.arcade.resources.lang.LanguageEntry
import net.casual.arcade.utils.ComponentUtils.getTranslationKeyOf
import net.minecraft.client.gui.Font
import net.minecraft.network.chat.Component
import net.minecraft.util.Mth
import org.apache.commons.text.StringEscapeUtils

public object SpacingUtils {
    public fun getTranslatableCentreSpacing(
        font: Font,
        foreground: Component,
        background: Component,
        key: String = getTranslationKeyOf(foreground)
    ): Pair<LanguageEntry, LanguageEntry> {
        val (first, second) = getCentreSpacingUnicode(font, foreground, background)
        return LanguageEntry("${key}.space.1", first) to LanguageEntry("${key}.space.2", second)
    }

    public fun getTranslatableNegativeWidth(
        font: Font,
        component: Component,
        key: String = getTranslationKeyOf(component)
    ): LanguageEntry {
        val unicode = getNegativeWidthUnicode(font, component)
        return LanguageEntry("${key}.negativeWidth", unicode)
    }

    public fun getTranslatableWidthDifference(
        font: Font,
        first: Component,
        second: Component,
        key: String = "${getTranslationKeyOf(first)}.difference.${getTranslationKeyOf(second).substringAfterLast('.')}"
    ): LanguageEntry {
        val unicode = getWidthDifferenceUnicode(font, first, second)
        return LanguageEntry(key, unicode)
    }

    public fun getCentreSpacingUnicode(
        font: Font,
        foreground: Component,
        background: Component
    ): Pair<String, String> {
        val (firstSpace, secondSpace) = getCentreSpacing(font, foreground, background)
        val first = SpacingFontResources.spaced(firstSpace)
        val second = SpacingFontResources.spaced(secondSpace)

        return StringEscapeUtils.escapeJava(first.string) to StringEscapeUtils.escapeJava(second.string)
    }

    public fun getMaxWidthUnicode(font: Font, component: List<Component>): String {
        val width = component.maxOf { getWidth(font, it) }
        val unicode = SpacingFontResources.spaced(width)
        return StringEscapeUtils.escapeJava(unicode.string)
    }

    public fun getNegativeWidthUnicode(font: Font, component: Component): String {
        val width = getWidth(font, component)
        val unicode = SpacingFontResources.spaced(-width)
        return StringEscapeUtils.escapeJava(unicode.string)
    }

    public fun getWidthDifferenceUnicode(font: Font, first: Component, second: Component): String {
        val firstWidth = getWidth(font, first)
        val secondWidth = getWidth(font, second)
        val unicode = SpacingFontResources.spaced(firstWidth - secondWidth)
        return StringEscapeUtils.escapeJava(unicode.string)
    }

    public fun getCentreSpacing(
        font: Font,
        foreground: Component,
        background: Component
    ): Pair<Int, Int> {
        val foregroundWidth = font.width(foreground)
        val backgroundWidth = font.width(background)
        val emptySpace = backgroundWidth - foregroundWidth
        val halfSpace = emptySpace / 2.0
        val halfSpaceFloor = Mth.floor(halfSpace)
        val halfSpaceCeil = Mth.ceil(halfSpace)

        val firstSpace = -halfSpaceFloor
        val secondSpace = -backgroundWidth + halfSpaceCeil
        return firstSpace to secondSpace
    }

    private fun getWidth(font: Font, component: Component): Int {
        return font.width(component)
    }
}