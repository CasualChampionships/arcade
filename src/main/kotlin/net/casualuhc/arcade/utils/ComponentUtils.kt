package net.casualuhc.arcade.utils

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.MutableComponent

@Suppress("unused")
object ComponentUtils {
    fun MutableComponent.command(command: String): MutableComponent {
        return this.withStyle { it.withClickEvent(ClickEvent(ClickEvent.Action.RUN_COMMAND, command)) }
    }

    fun MutableComponent.link(link: String): MutableComponent {
        return this.withStyle { it.withClickEvent(ClickEvent(ClickEvent.Action.OPEN_URL, link)) }
    }

    fun MutableComponent.bold(): MutableComponent {
        return this.withStyle(ChatFormatting.BOLD)
    }

    fun MutableComponent.unBold(): MutableComponent {
        return this.withStyle { it.withBold(false) }
    }

    fun MutableComponent.italicise(): MutableComponent {
        return this.withStyle(ChatFormatting.ITALIC)
    }

    fun MutableComponent.unItalicise(): MutableComponent {
        return this.withStyle { it.withItalic(false) }
    }

    fun MutableComponent.red(): MutableComponent {
        return this.withStyle(ChatFormatting.RED)
    }

    fun MutableComponent.crimson(): MutableComponent {
        return this.withStyle(ChatFormatting.DARK_RED)
    }

    fun MutableComponent.navy(): MutableComponent {
        return this.withStyle(ChatFormatting.DARK_BLUE)
    }

    fun MutableComponent.blue(): MutableComponent {
        return this.withStyle(ChatFormatting.BLUE)
    }

    fun MutableComponent.aqua(): MutableComponent {
        return this.withStyle(ChatFormatting.AQUA)
    }

    fun MutableComponent.teal(): MutableComponent {
        return this.withStyle(ChatFormatting.DARK_AQUA)
    }

    fun MutableComponent.gold(): MutableComponent {
        return this.withStyle(ChatFormatting.GOLD)
    }

    fun MutableComponent.pink(): MutableComponent {
        return this.withStyle(ChatFormatting.LIGHT_PURPLE)
    }

    fun MutableComponent.purple(): MutableComponent {
        return this.withStyle(ChatFormatting.DARK_PURPLE)
    }

    fun MutableComponent.lime(): MutableComponent {
        return this.withStyle(ChatFormatting.GREEN)
    }

    fun MutableComponent.green(): MutableComponent {
        return this.withStyle(ChatFormatting.DARK_GREEN)
    }

    fun MutableComponent.yellow(): MutableComponent {
        return this.withStyle(ChatFormatting.YELLOW)
    }

    fun MutableComponent.white(): MutableComponent {
        return this.withStyle(ChatFormatting.WHITE)
    }
}