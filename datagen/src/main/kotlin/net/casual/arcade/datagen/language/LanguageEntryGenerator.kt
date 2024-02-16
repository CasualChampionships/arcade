package net.casual.arcade.datagen.language

import net.casual.arcade.datagen.utils.SpacingUtils
import net.minecraft.client.gui.Font
import net.minecraft.network.chat.Component

interface LanguageEntryGenerator {
    fun run(font: Font, collection: MutableCollection<LanguageEntry>)
}

class CentredSpacingGenerator(
    private val foreground: Component,
    private val background: Component
): LanguageEntryGenerator {
    override fun run(font: Font, collection: MutableCollection<LanguageEntry>) {
        val (first, second) = SpacingUtils.getTranslatableCentreSpacing(font, this.foreground, this.background)
        collection.add(first)
        collection.add(second)
    }
}

class NegativeWidthGenerator(
    private val component: Component
): LanguageEntryGenerator {
    override fun run(font: Font, collection: MutableCollection<LanguageEntry>) {
        collection.add(SpacingUtils.getTranslatableNegativeWidth(font, this.component))
    }
}