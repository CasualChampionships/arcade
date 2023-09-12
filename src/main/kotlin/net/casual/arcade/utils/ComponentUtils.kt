package net.casual.arcade.utils

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import net.casual.arcade.commands.hidden.HiddenCommand
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.*
import net.minecraft.server.level.ServerPlayer
import org.jetbrains.annotations.ApiStatus.Experimental
import java.util.*
import java.util.function.Consumer

public object ComponentUtils {
    private val formattingByColour = Int2ObjectOpenHashMap<ChatFormatting>()

    init {
        for (formatting in ChatFormatting.values()) {
            val colour = formatting.color ?: continue
            this.formattingByColour[colour] = formatting
        }
    }

    @Experimental
    @JvmStatic
    public fun MutableComponent.singleUseFunction(consumer: Consumer<ServerPlayer>) {
        this.function { context ->
            consumer.accept(context.player)
            context.remove()
        }
    }

    @Experimental
    @JvmStatic
    public fun MutableComponent.function(consumer: Consumer<ServerPlayer>): MutableComponent {
        return this.function { consumer.accept(it.player) }
    }

    @Experimental
    @JvmStatic
    public fun MutableComponent.function(command: HiddenCommand): MutableComponent {
        return this.command(CommandUtils.registerHiddenCommand(command))
    }

    @JvmStatic
    public fun MutableComponent.command(command: String): MutableComponent {
        return this.withStyle { it.withClickEvent(ClickEvent(ClickEvent.Action.RUN_COMMAND, command)) }
    }

    @JvmStatic
    public fun MutableComponent.link(link: String): MutableComponent {
        return this.withStyle { it.withClickEvent(ClickEvent(ClickEvent.Action.OPEN_URL, link)) }
    }

    @JvmStatic
    public fun MutableComponent.bold(): MutableComponent {
        return this.withStyle(ChatFormatting.BOLD)
    }

    @JvmStatic
    public fun MutableComponent.unbold(): MutableComponent {
        return this.withStyle { it.withBold(false) }
    }

    @JvmStatic
    public fun MutableComponent.italicise(): MutableComponent {
        return this.withStyle(ChatFormatting.ITALIC)
    }

    @JvmStatic
    public fun MutableComponent.unitalicise(): MutableComponent {
        return this.withStyle { it.withItalic(false) }
    }

    @JvmStatic
    public fun MutableComponent.red(): MutableComponent {
        return this.withStyle(ChatFormatting.RED)
    }

    @JvmStatic
    public fun MutableComponent.crimson(): MutableComponent {
        return this.withStyle(ChatFormatting.DARK_RED)
    }

    @JvmStatic
    public fun MutableComponent.navy(): MutableComponent {
        return this.withStyle(ChatFormatting.DARK_BLUE)
    }

    @JvmStatic
    public fun MutableComponent.blue(): MutableComponent {
        return this.withStyle(ChatFormatting.BLUE)
    }

    @JvmStatic
    public fun MutableComponent.aqua(): MutableComponent {
        return this.withStyle(ChatFormatting.AQUA)
    }

    @JvmStatic
    public fun MutableComponent.teal(): MutableComponent {
        return this.withStyle(ChatFormatting.DARK_AQUA)
    }

    @JvmStatic
    public fun MutableComponent.gold(): MutableComponent {
        return this.withStyle(ChatFormatting.GOLD)
    }

    @JvmStatic
    public fun MutableComponent.pink(): MutableComponent {
        return this.withStyle(ChatFormatting.LIGHT_PURPLE)
    }

    @JvmStatic
    public fun MutableComponent.purple(): MutableComponent {
        return this.withStyle(ChatFormatting.DARK_PURPLE)
    }

    @JvmStatic
    public fun MutableComponent.lime(): MutableComponent {
        return this.withStyle(ChatFormatting.GREEN)
    }

    @JvmStatic
    public fun MutableComponent.green(): MutableComponent {
        return this.withStyle(ChatFormatting.DARK_GREEN)
    }

    @JvmStatic
    public fun MutableComponent.yellow(): MutableComponent {
        return this.withStyle(ChatFormatting.YELLOW)
    }

    @JvmStatic
    public fun MutableComponent.white(): MutableComponent {
        return this.withStyle(ChatFormatting.WHITE)
    }

    @JvmStatic
    public fun MutableComponent.colour(colour: Int): MutableComponent? {
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
            formats.add(ChatFormatting.BOLD)
        }
        if (this.isItalic) {
            formats.add(ChatFormatting.ITALIC)
        }
        if (this.isObfuscated) {
            formats.add(ChatFormatting.OBFUSCATED)
        }
        if (this.isStrikethrough) {
            formats.add(ChatFormatting.STRIKETHROUGH)
        }
        if (this.isUnderlined) {
            formats.add(ChatFormatting.UNDERLINE)
        }
        return formats
    }

    private class StringifyVisitor: FormattedText.StyledContentConsumer<Unit> {
        private val builder = StringBuilder()

        override fun accept(style: Style, string: String): Optional<Unit> {
            for (format in style.toChatFormatting()) {
                this.builder.append(ChatFormatting.PREFIX_CODE)
                this.builder.append(format.char)
            }
            this.builder.append(string)
            this.builder.append(ChatFormatting.PREFIX_CODE)
            this.builder.append(ChatFormatting.RESET.char)
            return Optional.empty()
        }

        override fun toString(): String {
            return this.builder.toString()
        }
    }
}