package net.casual.arcade.utils

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.FormattedText
import net.minecraft.network.chat.Style
import java.util.*

object TextUtils {
    private val formattingByColour = Int2ObjectOpenHashMap<ChatFormatting>()

    init {
        for (formatting in ChatFormatting.values()) {
            val colour = formatting.color ?: continue
            this.formattingByColour[colour] = formatting
        }
    }

    fun Component.toFormattedString(): String {
        val visitor = Visitor()
        this.visit(visitor, Style.EMPTY)
        return visitor.toString()
    }

    fun Style.toChatFormatting(): Set<ChatFormatting> {
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

    private class Visitor: FormattedText.StyledContentConsumer<Unit> {
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