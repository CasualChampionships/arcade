/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.scoreboard

import net.minecraft.ChatFormatting

public enum class TeamColor(
    public val formatting: ChatFormatting
) {
    Black(ChatFormatting.BLACK),
    Navy(ChatFormatting.DARK_BLUE),
    Green(ChatFormatting.DARK_GREEN),
    Teal(ChatFormatting.DARK_AQUA),
    Red(ChatFormatting.DARK_RED),
    Purple(ChatFormatting.DARK_PURPLE),
    Orange(ChatFormatting.GOLD),
    Silver(ChatFormatting.GRAY),
    Gray(ChatFormatting.DARK_GRAY),
    Blue(ChatFormatting.BLUE),
    Lime(ChatFormatting.GREEN),
    Aqua(ChatFormatting.AQUA),
    Crimson(ChatFormatting.RED),
    Pink(ChatFormatting.LIGHT_PURPLE),
    Yellow(ChatFormatting.YELLOW),
    White(ChatFormatting.WHITE);

    public companion object {
        public val grays: Set<TeamColor> = setOf(Black, Silver, Gray, White)
        public val colorful: Set<TeamColor> = entries.toSet() - grays

        public fun from(formatting: ChatFormatting): TeamColor {
            for (color in entries) {
                if (color.formatting == formatting) {
                    return color
                }
            }
            return White
        }
    }
}