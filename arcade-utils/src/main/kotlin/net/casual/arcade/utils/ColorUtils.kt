/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils

import net.minecraft.util.ARGB
import java.awt.Color

public object ColorUtils {
    public fun greyscale(color: Int): Int {
        val red = (color shr 16) and 0xFF
        val green = (color shr 8) and 0xFF
        val blue = color and 0xFF

        val grey = (0.21 * red + 0.72 * green + 0.07 * blue).toInt()
        return (grey shl 16) or (grey shl 8) or grey
    }

    /**
     * Linearly interpolates two hues.
     *
     * @param delta The delta between the hues.
     * @param hue0 The starting hue, in degrees, between 0-360.
     * @param hue1 The ending hue, in degrees, between 0-360.
     */
    public fun lerp(delta: Float, hue0: Float, hue1: Float): Int {
        val hue = (1 - delta) * hue0 / 360.0F + delta * hue1 / 360.0F
        return Color.HSBtoRGB(hue, 1.0F, 1.0F)
    }

    public fun heatmap(delta: Float): Int {
        if (delta >= 0.75F) {
            val d0 = ((delta - 0.75F) * 4).coerceIn(0.0F..1.0F)
            return lerp(d0, 60.0F, 120.0F)
        }
        if (delta >= 0.25F) {
            val d0 = ((delta - 0.25F) * 2).coerceIn(0.0F..1.0F)
            return lerp(d0, 0.0F, 60.0F)
        }
        return ARGB.color(0xFF, 0x00, 0x00)
    }
}