/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import net.casual.arcade.utils.ComponentUtils.ComponentGenerator
import net.minecraft.ChatFormatting
import net.minecraft.ChatFormatting.*
import net.minecraft.network.chat.*
import net.minecraft.network.chat.HoverEvent.EntityTooltipInfo
import net.minecraft.network.chat.contents.PlainTextContents
import net.minecraft.network.chat.contents.TranslatableContents
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.ItemStack
import java.net.URI
import java.util.*
import kotlin.reflect.KProperty

public object ComponentUtils {
    public val SPACING_FONT: ResourceLocation = ResourceUtils.arcade("spacing")
    public val MINI_FONT: ResourceLocation = ResourceUtils.arcade("mini_minecraft")

    private val formattingByColour = Int2ObjectOpenHashMap<ChatFormatting>()
    private val formattingToName: EnumMap<ChatFormatting, String>

    private val charWidths = Int2IntOpenHashMap()

    init {
        for (formatting in ChatFormatting.entries) {
            val colour = formatting.color ?: continue
            this.formattingByColour[colour] = formatting
        }
        this.formattingToName = EnumUtils.mapOf(
            BLACK to "Black",
            DARK_BLUE to "Navy",
            DARK_GREEN to "Green",
            DARK_AQUA to "Teal",
            DARK_RED to "Red",
            DARK_PURPLE to "Purple",
            GOLD to "Orange",
            GRAY to "Stone",
            DARK_GRAY to "Grey",
            BLUE to "Blue",
            GREEN to "Lime",
            AQUA to "Aqua",
            RED to "Crimson",
            LIGHT_PURPLE to "Pink",
            YELLOW to "Yellow",
            WHITE to "White",
        )

        this.charWidths.defaultReturnValue(6)
        this.charWidths.put(32, 4)
        this.charWidths.put(33, 2)
        this.charWidths.put(34, 4)
        this.charWidths.put(39, 2)
        this.charWidths.put(40, 4)
        this.charWidths.put(41, 4)
        this.charWidths.put(42, 4)
        this.charWidths.put(44, 2)
        this.charWidths.put(46, 2)
        this.charWidths.put(58, 2)
        this.charWidths.put(59, 2)
        this.charWidths.put(60, 5)
        this.charWidths.put(62, 5)
        this.charWidths.put(64, 7)
        this.charWidths.put(73, 4)
        this.charWidths.put(91, 4)
        this.charWidths.put(93, 4)
        this.charWidths.put(96, 3)
        this.charWidths.put(102, 5)
        this.charWidths.put(105, 2)
        this.charWidths.put(107, 5)
        this.charWidths.put(108, 3)
        this.charWidths.put(116, 4)
        this.charWidths.put(123, 4)
        this.charWidths.put(124, 2)
        this.charWidths.put(125, 4)
        this.charWidths.put(126, 7)
    }

    public fun negativeWidthOf(component: Component): MutableComponent {
        val key = getTranslationKeyOf(component)
        return Component.translatable("$key.negativeWidth").withSpacingFont()
    }

    public fun widthDifferenceBetween(first: Component, second: Component): MutableComponent {
        val key = "${getTranslationKeyOf(first)}.difference.${getTranslationKeyOf(second).substringAfterLast('.')}"
        return Component.translatable(key).withSpacingFont()
    }

    public fun getTranslationKeyOf(component: Component): String {
        val contents = component.contents
        if (contents !is TranslatableContents) {
            throw IllegalStateException()
        }
        return contents.key
    }

    public fun widthOf(component: Component): Int {
        var width = 0
        component.visit({ style, content ->
            width += this.widthOf(content)
            if (style.isBold) {
                width += content.length
            }

            Optional.empty<Unit>()
        }, Style.EMPTY)
        return width
    }

    public fun widthOf(string: String): Int {
        return string.sumOf(this::widthOf)
    }

    public fun widthOf(char: Char): Int {
        return this.charWidths.get(char.code)
    }

    @JvmStatic
    @Deprecated(
        "Use Component.literal() instead",
        ReplaceWith(
            "Component.literal(this)",
            "net.minecraft.network.chat.Component", "net.casual.arcade.utils.ComponentUtils.literal"
        )
    )
    public fun String.literal(): MutableComponent {
        return Component.literal(this)
    }

    @JvmStatic
    public fun Component.isEmpty(): Boolean {
        return this.contents == PlainTextContents.EMPTY && this.siblings.all { it.isEmpty() }
    }

    @JvmStatic
    public fun MutableComponent.appendSpaced(
        other: Component,
        space: Component = CommonComponents.SPACE
    ): MutableComponent {
        if (other.isEmpty()) {
            return this
        }
        if (this.isEmpty()) {
            return this.append(other)
        }
        return this.append(space).append(other)
    }

    @JvmStatic
    public fun Component.wrap(): MutableComponent {
        return Component.empty().append(this)
    }

    @JvmStatic
    public fun MutableComponent.command(command: String): MutableComponent {
        return this.withStyle { it.withClickEvent(ClickEvent.RunCommand(command)) }
    }

    @JvmStatic
    public fun MutableComponent.suggestCommand(command: String): MutableComponent {
        return this.withStyle { it.withClickEvent(ClickEvent.SuggestCommand(command)) }
    }

    @JvmStatic
    public fun MutableComponent.link(link: String): MutableComponent {
        return this.link(URI.create(link))
    }

    @JvmStatic
    public fun MutableComponent.link(link: URI): MutableComponent {
        return this.withStyle { it.withClickEvent(ClickEvent.OpenUrl(link)) }
    }

    @JvmStatic
    public fun MutableComponent.hover(string: String): MutableComponent {
        return this.hover(Component.literal(string))
    }

    @JvmStatic
    public fun MutableComponent.hover(component: Component): MutableComponent {
        return this.withStyle { it.withHoverEvent(HoverEvent.ShowText(component)) }
    }

    @JvmStatic
    public fun MutableComponent.hover(entity: Entity): MutableComponent {
        return this.withStyle {
            it.withHoverEvent(HoverEvent.ShowEntity(EntityTooltipInfo(entity.type, entity.uuid, entity.name)))
        }
    }

    @JvmStatic
    public fun MutableComponent.hover(item: ItemStack): MutableComponent {
        return this.withStyle { it.withHoverEvent(HoverEvent.ShowItem(item)) }
    }

    @JvmStatic
    public fun MutableComponent.bold(): MutableComponent {
        return this.withStyle(BOLD)
    }

    @JvmStatic
    public fun MutableComponent.unbold(): MutableComponent {
        return this.withStyle { it.withBold(false) }
    }

    @JvmStatic
    public fun MutableComponent.underline(): MutableComponent {
        return this.withStyle(UNDERLINE)
    }

    @JvmStatic
    public fun MutableComponent.noUnderline(): MutableComponent {
        return this.withStyle { it.withUnderlined(false) }
    }

    @JvmStatic
    public fun MutableComponent.italicise(): MutableComponent {
        return this.withStyle(ITALIC)
    }

    @JvmStatic
    public fun MutableComponent.unitalicise(): MutableComponent {
        return this.withStyle { it.withItalic(false) }
    }

    @JvmStatic
    public fun MutableComponent.strikethrough(): MutableComponent {
        return this.withStyle(STRIKETHROUGH)
    }

    @JvmStatic
    public fun MutableComponent.unstrikethrough(): MutableComponent {
        return this.withStyle { it.withStrikethrough(false) }
    }

    @JvmStatic
    public fun ChatFormatting.prettyName(): String {
        return formattingToName[this] ?: this.getName()
    }

    @JvmStatic
    public fun colorToFormatting(color: Int): ChatFormatting? {
        return this.formattingByColour[color]
    }

    @JvmStatic
    public fun MutableComponent.red(): MutableComponent {
        return this.withStyle(RED)
    }

    @JvmStatic
    public fun MutableComponent.crimson(): MutableComponent {
        return this.withStyle(DARK_RED)
    }

    @JvmStatic
    public fun MutableComponent.navy(): MutableComponent {
        return this.withStyle(DARK_BLUE)
    }

    @JvmStatic
    public fun MutableComponent.blue(): MutableComponent {
        return this.withStyle(BLUE)
    }

    @JvmStatic
    public fun MutableComponent.aqua(): MutableComponent {
        return this.withStyle(AQUA)
    }

    @JvmStatic
    public fun MutableComponent.teal(): MutableComponent {
        return this.withStyle(DARK_AQUA)
    }

    @JvmStatic
    public fun MutableComponent.gold(): MutableComponent {
        return this.withStyle(GOLD)
    }

    @JvmStatic
    public fun MutableComponent.pink(): MutableComponent {
        return this.withStyle(LIGHT_PURPLE)
    }

    @JvmStatic
    public fun MutableComponent.purple(): MutableComponent {
        return this.withStyle(DARK_PURPLE)
    }

    @JvmStatic
    public fun MutableComponent.lime(): MutableComponent {
        return this.withStyle(GREEN)
    }

    @JvmStatic
    public fun MutableComponent.green(): MutableComponent {
        return this.withStyle(DARK_GREEN)
    }

    @JvmStatic
    public fun MutableComponent.yellow(): MutableComponent {
        return this.withStyle(YELLOW)
    }

    @JvmStatic
    public fun MutableComponent.white(): MutableComponent {
        return this.withStyle(WHITE)
    }

    @JvmStatic
    public fun MutableComponent.grey(): MutableComponent {
        return this.withStyle(GRAY)
    }

    @JvmStatic
    public fun MutableComponent.black(): MutableComponent {
        return this.withStyle(BLACK)
    }

    @JvmStatic
    public fun MutableComponent.color(color: Int): MutableComponent {
        return this.withStyle { it.withColor(color) }
    }

    @JvmStatic
    public fun MutableComponent.shadow(color: Int): MutableComponent {
        return this.withStyle { it.withShadowColor(color) }
    }

    @JvmStatic
    public fun Iterable<Component>.join(
        separator: Component = Component.literal(", "),
        prefix: Component? = null,
        postfix: Component? = null
    ): MutableComponent {
        return this.joinToComponent(separator, prefix, postfix) { it }
    }

    @JvmStatic
    public fun <T> Iterable<T>.joinToComponent(
        separator: Component = Component.literal(", "),
        prefix: Component? = null,
        postfix: Component? = null,
        transformer: (T) -> Component
    ): MutableComponent {
        val component = Component.empty()
        if (prefix != null) {
            component.append(prefix)
        }
        val iterator = this.iterator()
        while (iterator.hasNext()) {
            component.append(transformer(iterator.next()))
            if (iterator.hasNext()) {
                component.append(separator)
            }
        }
        if (postfix != null) {
            component.append(postfix)
        }
        return component
    }

    @JvmStatic
    public fun Component.greyscale(): MutableComponent {
        return this.mapColours { colour ->
            colour ?: return@mapColours null
            TextColor.fromRgb(ColorUtils.greyscale(colour.value))
        }
    }

    @JvmStatic
    public fun Component.mapColours(mapper: (TextColor?) -> TextColor?): MutableComponent {
        val copy = this.plainCopy()
        copy.style = this.style.withColor(mapper(this.style.color))
        for (sibling in this.siblings) {
            val mapped = sibling.mapColours(mapper)
            copy.append(mapped)
        }
        return copy
    }

    @JvmStatic
    public fun Component.toFormattedString(): String {
        val visitor = StringifyVisitor()
        this.visit(visitor, Style.EMPTY)
        return visitor.toString()
    }

    @JvmStatic
    public fun Style.toChatFormatting(): Set<ChatFormatting> {
        if (this.isEmpty) {
            return emptySet()
        }

        val formats = HashSet<ChatFormatting>()
        val color = this.color
        if (color !== null) {
            // Technically we could use the name, but this is probably better...
            val formatting = colorToFormatting(color.value)
            if (formatting != null) {
                formats.add(formatting)
            }
        }

        if (this.isBold) {
            formats.add(BOLD)
        }
        if (this.isItalic) {
            formats.add(ITALIC)
        }
        if (this.isObfuscated) {
            formats.add(OBFUSCATED)
        }
        if (this.isStrikethrough) {
            formats.add(STRIKETHROUGH)
        }
        if (this.isUnderlined) {
            formats.add(UNDERLINE)
        }
        return formats
    }

    public fun MutableComponent.withFont(font: ResourceLocation): MutableComponent {
        return this.withStyle { it.withFont(font) }
    }

    public fun MutableComponent.withDefaultFont(): MutableComponent {
        return this.withFont(Style.DEFAULT_FONT)
    }

    public fun MutableComponent.withShiftedDownFont(shift: Int): MutableComponent {
        return this.withFont(ResourceUtils.arcade("default_shifted_down_${shift}"))
    }

    public fun MutableComponent.withMiniShiftedDownFont(shift: Int): MutableComponent {
        return this.withFont(ResourceUtils.arcade("mini_shifted_down_${shift}"))
    }

    public fun MutableComponent.withSpacingFont(): MutableComponent {
        return this.withFont(SPACING_FONT)
    }

    public fun MutableComponent.shadowless(): MutableComponent {
        return this.shadow(0)
    }

    public fun MutableComponent.mini(): MutableComponent {
        return this.withFont(MINI_FONT)
    }

    public fun literal(key: String, modifier: (MutableComponent.() -> Unit)? = null): ConstantComponentGenerator {
        return ConstantComponentGenerator(key, Component::literal, modifier)
    }

    public fun translatable(key: String, modifier: (MutableComponent.() -> Unit)? = null): ConstantComponentGenerator {
        return ConstantComponentGenerator(key, Component::translatable, modifier)
    }

    public fun translatableWithArgs(key: String, modifier: (MutableComponent.() -> Unit)? = null): ComponentGenerator {
        return ComponentGenerator {
            val component = Component.translatable(key, *it)
            modifier?.invoke(component)
            component
        }
    }

    public fun interface ComponentGenerator {
        public fun generate(vararg args: Any?): MutableComponent
    }

    public class ConstantComponentGenerator(
        private val key: String,
        private val supplier: (String) -> MutableComponent,
        private val consumer: (MutableComponent.() -> Unit)?,
    ) {
        public fun generate(): MutableComponent {
            val component = this.supplier(this.key)
            this.consumer?.invoke(component)
            return component
        }

        public fun with(mutator: MutableComponent.() -> Unit): ConstantComponentGenerator {
            val consumer = this.consumer
            return if (consumer != null) {
                ConstantComponentGenerator(this.key, this.supplier) {
                    consumer()
                    mutator()
                }
            } else {
                ConstantComponentGenerator(this.key, this.supplier, mutator)
            }
        }

        public operator fun getValue(any: Any, property: KProperty<*>): MutableComponent {
            return this.generate()
        }
    }

    private class StringifyVisitor: FormattedText.StyledContentConsumer<Unit> {
        private val builder = StringBuilder()

        override fun accept(style: Style, string: String): Optional<Unit> {
            for (format in style.toChatFormatting()) {
                this.builder.append(PREFIX_CODE)
                this.builder.append(format.char)
            }
            this.builder.append(string)
            this.builder.append(PREFIX_CODE)
            this.builder.append(RESET.char)
            return Optional.empty()
        }

        override fun toString(): String {
            return this.builder.toString()
        }
    }
}