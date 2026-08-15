/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.pack.utils

import net.casual.arcade.utils.component.WidthResolver
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import java.util.*

public object MiniLiteralWidthResolver: WidthResolver {
    public fun width(char: Char): Int {
        return when (char) {
            '!', ':', '.', '\'', '|' -> 2
            '(', ')', '[', ']', ';', ',', '`' -> 3
            '*', '"' -> 4
            ' ' -> 5
            else -> 6
        }
    }

    public fun width(codepoint: Int): Int {
        return this.width(codepoint.toChar())
    }

    override fun width(codepoint: Int, style: Style): Int {
        return this.width(codepoint) + if (style.isBold) 1 else 0
    }

    public fun width(string: String): Int {
        return string.sumOf(this::width)
    }

    public fun width(component: Component): Int {
        var width = 0
        component.visit({ style, content ->
            width += this.width(content)
            if (style.isBold) {
                width += content.length
            }

            Optional.empty<Unit>()
        }, Style.EMPTY)
        return width
    }
}