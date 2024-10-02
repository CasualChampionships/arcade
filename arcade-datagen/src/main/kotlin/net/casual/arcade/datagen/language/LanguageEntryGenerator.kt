package net.casual.arcade.datagen.language

import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue.Consumer
import net.casual.arcade.datagen.utils.SpacingUtils
import net.casual.arcade.resources.lang.LanguageEntry
import net.minecraft.client.gui.Font
import net.minecraft.network.chat.Component

public interface LanguageEntryGenerator {
    public fun run(font: Font, lang: String, consumer: Consumer<LanguageEntry>)
}

public class CentredSpacingGenerator(
    private val foreground: Component,
    private val background: Component
): LanguageEntryGenerator {
    override fun run(font: Font, lang: String, consumer: Consumer<LanguageEntry>) {
        val (first, second) = SpacingUtils.getTranslatableCentreSpacing(font, this.foreground, this.background)
        consumer.accept(first)
        consumer.accept(second)
    }
}

public class NegativeWidthGenerator(
    private val component: Component
): LanguageEntryGenerator {
    override fun run(font: Font, lang: String, consumer: Consumer<LanguageEntry>) {
        consumer.accept(SpacingUtils.getTranslatableNegativeWidth(font, this.component))
    }
}

public class WidthDifferenceGenerator(
    private val first: Component,
    private val second: Component
): LanguageEntryGenerator {
    override fun run(font: Font, lang: String, consumer: Consumer<LanguageEntry>) {
        consumer.accept(SpacingUtils.getTranslatableWidthDifference(font, this.first, this.second))
    }
}

public class MaxWidthGenerator(
    private val key: String,
    private val components: List<Component>
): LanguageEntryGenerator {
    public constructor(key: String, vararg components: Component): this(key, components.toList())

    override fun run(font: Font, lang: String, consumer: Consumer<LanguageEntry>) {
        consumer.accept(LanguageEntry(this.key, SpacingUtils.getMaxWidthUnicode(font, this.components)))
    }
}