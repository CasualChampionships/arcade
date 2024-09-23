package net.casual.arcade.datagen.language

import net.casual.arcade.datagen.utils.SpacingUtils
import net.casual.arcade.resources.lang.LanguageEntry
import net.minecraft.client.gui.Font
import net.minecraft.network.chat.Component

public interface LanguageEntryGenerator {
    public fun run(font: Font, collection: MutableCollection<LanguageEntry>)
}

public class CentredSpacingGenerator(
    private val foreground: Component,
    private val background: Component
): LanguageEntryGenerator {
    override fun run(font: Font, collection: MutableCollection<LanguageEntry>) {
        val (first, second) = SpacingUtils.getTranslatableCentreSpacing(font, this.foreground, this.background)
        collection.add(first)
        collection.add(second)
    }
}

public class NegativeWidthGenerator(
    private val component: Component
): LanguageEntryGenerator {
    override fun run(font: Font, collection: MutableCollection<LanguageEntry>) {
        collection.add(SpacingUtils.getTranslatableNegativeWidth(font, this.component))
    }
}

public class WidthDifferenceGenerator(
    private val first: Component,
    private val second: Component
): LanguageEntryGenerator {
    override fun run(font: Font, collection: MutableCollection<LanguageEntry>) {
        collection.add(SpacingUtils.getTranslatableWidthDifference(font, this.first, this.second))
    }
}