package net.casual.arcade.datagen.utils

import net.casual.arcade.datagen.language.LanguageEntry
import net.casual.arcade.utils.ComponentUtils
import net.minecraft.client.gui.Font
import net.minecraft.network.chat.Component
import net.minecraft.util.Mth
import org.apache.commons.text.StringEscapeUtils

public object SpacingUtils {
    public fun getTranslatableCentreSpacing(
        font: Font,
        foreground: Component,
        background: Component,
        key: String = ComponentUtils.getTranslationKeyOf(foreground)
    ): Pair<LanguageEntry, LanguageEntry> {
        val (first, second) = getCentreSpacingUnicode(font, foreground, background)
        return LanguageEntry("${key}.space.1", first) to LanguageEntry("${key}.space.2", second)
    }

    public fun getTranslatableNegativeWidth(
        font: Font,
        component: Component,
        key: String = ComponentUtils.getTranslationKeyOf(component)
    ): LanguageEntry {
        val unicode = getNegativeWidthUnicode(font, component)
        return LanguageEntry("${key}.negativeWidth", unicode)
    }

    public fun getCentreSpacingUnicode(
        font: Font,
        foreground: Component,
        background: Component
    ): Pair<String, String> {
        val (firstSpace, secondSpace) = getCentreSpacing(font, foreground, background)
        val first = ComponentUtils.space(firstSpace)
        val second = ComponentUtils.space(secondSpace)

        return StringEscapeUtils.escapeJava(first.string) to StringEscapeUtils.escapeJava(second.string)
    }

    public fun getNegativeWidthUnicode(font: Font, component: Component): String {
        val width = getWidth(font, component)
        val unicode = ComponentUtils.space(-width)
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