/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.component

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import java.util.*

public object LiteralWidthResolver {
    private val widths = Int2IntOpenHashMap()

    init {
        this.initialize()
    }

    /**
     * Calculates the width of the given [char] in gui pixels.
     *
     * This assumes the use of [Style.DEFAULT_FONT] as well as
     * only using ascii characters.
     *
     * @param char The character to calculate the width of.
     * @return The calculated width of the [char] in gui pixels.
     */
    public fun width(char: Char): Int {
        return this.widths.get(char.code)
    }

    /**
     * Calculates the width of the given [codepoint] in gui pixels.
     *
     * This assumes the use of [Style.DEFAULT_FONT] as well as
     * only using ascii characters.
     *
     * @param codepoint The codepoint to calculate the width of.
     * @return The calculated width of the [codepoint] in gui pixels.
     */
    public fun width(codepoint: Int): Int {
        return this.widths.get(codepoint)
    }

    /**
     * Calculates the width of the given [string] in gui pixels.
     *
     * This assumes the use of [Style.DEFAULT_FONT] as well as
     * only using ascii characters.
     *
     * @param string The string to calculate the width of.
     * @return The calculated width of the [string] in gui pixels.
     */
    public fun width(string: String): Int {
        return string.sumOf(this::width)
    }

    /**
     * Calculates the width of the given [component] in gui pixels.
     *
     * This only works on *literal* components,
     * and assumes that the component is using the
     * [Style.DEFAULT_FONT].
     *
     * This also assumes that only ascii characters are
     * used within the [component].
     *
     * @param component The component to calculate the width of.
     * @return The calculated width of the [component]
     */
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

    private fun initialize() {
        this.widths.defaultReturnValue(6)
        this.widths.put(32, 4)
        this.widths.put(33, 2)
        this.widths.put(34, 4)
        this.widths.put(39, 2)
        this.widths.put(40, 4)
        this.widths.put(41, 4)
        this.widths.put(42, 4)
        this.widths.put(44, 2)
        this.widths.put(46, 2)
        this.widths.put(58, 2)
        this.widths.put(59, 2)
        this.widths.put(60, 5)
        this.widths.put(62, 5)
        this.widths.put(64, 7)
        this.widths.put(73, 4)
        this.widths.put(91, 4)
        this.widths.put(93, 4)
        this.widths.put(96, 3)
        this.widths.put(102, 5)
        this.widths.put(105, 2)
        this.widths.put(107, 5)
        this.widths.put(108, 3)
        this.widths.put(116, 4)
        this.widths.put(123, 4)
        this.widths.put(124, 2)
        this.widths.put(125, 4)
        this.widths.put(126, 7)
    }
}