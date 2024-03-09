package net.casual.arcade.utils

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import net.casual.arcade.Arcade
import net.casual.arcade.commands.hidden.HiddenCommand
import net.casual.arcade.scheduler.MinecraftTimeDuration
import net.casual.arcade.utils.TimeUtils.Minutes
import net.minecraft.ChatFormatting
import net.minecraft.ChatFormatting.*
import net.minecraft.network.chat.*
import net.minecraft.network.chat.HoverEvent.Action.*
import net.minecraft.network.chat.HoverEvent.EntityTooltipInfo
import net.minecraft.network.chat.HoverEvent.ItemStackInfo
import net.minecraft.network.chat.contents.TranslatableContents
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.ItemStack
import org.jetbrains.annotations.ApiStatus.Experimental
import java.util.*
import java.util.function.Consumer
import kotlin.reflect.KProperty

public object ComponentUtils {
    public val SPACES_FONT: ResourceLocation = ResourceLocation("space", "spaces")
    public val DEFAULT_SHIFTED_DOWN_1_FONT: ResourceLocation = Arcade.id("default_shifted_down_1")
    public val DEFAULT_SHIFTED_DOWN_2_FONT: ResourceLocation = Arcade.id("default_shifted_down_2")
    public val DEFAULT_SHIFTED_DOWN_3_FONT: ResourceLocation = Arcade.id("default_shifted_down_3")
    public val DEFAULT_SHIFTED_DOWN_4_FONT: ResourceLocation = Arcade.id("default_shifted_down_4")
    public val DEFAULT_SHIFTED_DOWN_5_FONT: ResourceLocation = Arcade.id("default_shifted_down_5")

    private val formattingByColour = Int2ObjectOpenHashMap<ChatFormatting>()
    private val formattingToName = HashMap<ChatFormatting, String>()

    init {
        for (formatting in ChatFormatting.values()) {
            val colour = formatting.color ?: continue
            this.formattingByColour[colour] = formatting
        }
        this.formattingToName.apply {
            put(BLACK, "Black")
            put(DARK_BLUE, "Navy")
            put(DARK_GREEN, "Green")
            put(DARK_AQUA, "Teal")
            put(DARK_RED, "Red")
            put(DARK_PURPLE, "Purple")
            put(GOLD, "Orange")
            put(GRAY, "Stone")
            put(DARK_GRAY, "Grey")
            put(BLUE, "Blue")
            put(GREEN, "Lime")
            put(AQUA, "Aqua")
            put(RED, "Crimson")
            put(LIGHT_PURPLE, "Pink")
            put(YELLOW, "Yellow")
            put(WHITE, "White")
        }
    }

    public fun space(top: Int, bottom: Int): MutableComponent {
        return Component.translatable("space.$top/$bottom").withStyle { it.withFont(SPACES_FONT) }
    }

    public fun space(space: Int = 4): MutableComponent {
        val clamped = Mth.clamp(space, -8192, 8192)
        return Component.translatable("space.$clamped").withStyle { it.withFont(SPACES_FONT) }
    }

    public fun offset(offset: Int, component: Component): MutableComponent {
        val clamped = Mth.clamp(offset, -8192, 8192)
        return Component.translatable("offset.${clamped}", arrayOf(component)).withStyle { it.withFont(SPACES_FONT) }
    }

    public fun negativeWidthOf(component: Component): MutableComponent {
        val key = getTranslationKeyOf(component)
        return Component.translatable("$key.negativeWidth").withSpacesFont()
    }

    public fun getTranslationKeyOf(component: Component): String {
        val contents = component.contents
        if (contents !is TranslatableContents) {
            throw IllegalStateException()
        }
        return contents.key
    }

    @JvmStatic
    public fun String.literal(): MutableComponent {
        return Component.literal(this)
    }

    @Experimental
    @JvmStatic
    public fun MutableComponent.singleUseFunction(consumer: Consumer<ServerPlayer>): MutableComponent {
        return this.function { context ->
            consumer.accept(context.player)
            context.removeCommand()
        }
    }

    @Experimental
    @JvmStatic
    public fun MutableComponent.function(consumer: Consumer<ServerPlayer>): MutableComponent {
        return this.function { consumer.accept(it.player) }
    }

    @Experimental
    @JvmStatic
    public fun MutableComponent.function(timeout: MinecraftTimeDuration = 10.Minutes, command: HiddenCommand): MutableComponent {
        return this.command(CommandUtils.registerHiddenCommand(timeout, command))
    }

    @JvmStatic
    public fun MutableComponent.command(command: String): MutableComponent {
        return this.withStyle { it.withClickEvent(ClickEvent(ClickEvent.Action.RUN_COMMAND, command)) }
    }

    @JvmStatic
    public fun MutableComponent.suggestCommand(command: String): MutableComponent {
        return this.withStyle { it.withClickEvent(ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command)) }
    }

    @JvmStatic
    public fun MutableComponent.link(link: String): MutableComponent {
        return this.withStyle { it.withClickEvent(ClickEvent(ClickEvent.Action.OPEN_URL, link)) }
    }

    @JvmStatic
    public fun MutableComponent.hover(string: String): MutableComponent {
        return this.hover(string.literal())
    }

    @JvmStatic
    public fun MutableComponent.hover(component: Component): MutableComponent {
        return this.withStyle { it.withHoverEvent(HoverEvent(SHOW_TEXT, component)) }
    }

    @JvmStatic
    public fun MutableComponent.hover(entity: Entity): MutableComponent {
        return this.withStyle {
            it.withHoverEvent(HoverEvent(SHOW_ENTITY, EntityTooltipInfo(entity.type, entity.uuid, entity.name)))
        }
    }

    @JvmStatic
    public fun MutableComponent.hover(item: ItemStack): MutableComponent {
        return this.withStyle { it.withHoverEvent(HoverEvent(SHOW_ITEM, ItemStackInfo(item))) }
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
    public fun ChatFormatting.prettyName(): String {
        return formattingToName[this] ?: this.getName()
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
    public fun MutableComponent.colour(colour: Int): MutableComponent {
        return this.withStyle { it.withColor(colour) }
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
        val colour = this.color
        if (colour !== null) {
            // Technically we could use the name, but this is probably better...
            val formatting = formattingByColour[colour.value]
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

    public fun MutableComponent.withShiftedDown1Font(): MutableComponent {
        return this.withFont(DEFAULT_SHIFTED_DOWN_1_FONT)
    }

    public fun MutableComponent.withShiftedDown2Font(): MutableComponent {
        return this.withFont(DEFAULT_SHIFTED_DOWN_2_FONT)
    }

    public fun MutableComponent.withShiftedDown3Font(): MutableComponent {
        return this.withFont(DEFAULT_SHIFTED_DOWN_3_FONT)
    }

    public fun MutableComponent.withShiftedDown4Font(): MutableComponent {
        return this.withFont(DEFAULT_SHIFTED_DOWN_4_FONT)
    }

    public fun MutableComponent.withShiftedDown5Font(): MutableComponent {
        return this.withFont(DEFAULT_SHIFTED_DOWN_5_FONT)
    }

    public fun MutableComponent.withSpacesFont(): MutableComponent {
        return this.withFont(SPACES_FONT)
    }

    public fun MutableComponent.shadowless(): MutableComponent {
        return this.colour(0x4E5C24)
    }

    public fun literal(key: String, modifier: (MutableComponent.() -> Unit)? = null): ConstantComponentGenerator {
        return ConstantComponentGenerator(key, Component::literal, modifier)
    }

    public fun translatable(key: String, modifier: (MutableComponent.() -> Unit)? = null): ConstantComponentGenerator {
        return ConstantComponentGenerator(key, Component::translatable, modifier)
    }

    public fun translatableWithArgs(key: String, modifier: (MutableComponent.() -> Unit)? = null): ComponentGenerator {
        return ComponentGenerator {
            val component = Component.translatable(key, it)
            modifier?.invoke(component)
            component
        }
    }

    public fun interface ComponentGenerator {
        public fun generate(vararg args: Any?): Component
    }

    public class ConstantComponentGenerator(
        private val key: String,
        private val supplier: (String) -> MutableComponent,
        private val consumer: (MutableComponent.() -> Unit)?,
    ) {
        public operator fun getValue(any: Any, property: KProperty<*>): MutableComponent {
            val component = this.supplier(this.key)
            this.consumer?.invoke(component)
            return component
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