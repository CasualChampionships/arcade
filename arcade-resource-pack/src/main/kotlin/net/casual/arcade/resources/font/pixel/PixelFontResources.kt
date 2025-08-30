/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.resources.font.pixel

import net.casual.arcade.resources.font.IndexedFontResources
import net.casual.arcade.resources.font.spacing.SpacingFontResources
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.ComponentUtils.color
import net.casual.arcade.utils.ComponentUtils.wrap
import net.minecraft.network.chat.Component
import java.awt.image.BufferedImage

public object PixelFontResources: IndexedFontResources(ArcadeUtils.id("pixel")) {
    init {
        val texture = at("pixel.png")
        for (i in -256..256) {
            this.indexed { this.bitmap(texture, ascent = i, height = 256) }
        }
    }

    public fun pixel(height: Int): Component {
        require(height in -256..256) { "Invalid height for pixel" }
        return this.get(height + 256)
    }

    public fun from(image: BufferedImage, shift: Int = 0): Component {
        val component = Component.empty()
        for (y in 0..< image.height) {
            for (x in 0..< image.width) {
                if (x != 0) {
                    component.append(SpacingFontResources.spaced(-1))
                }
                val color = image.getRGB(x, y)
                val pixel = this.pixel(image.height - y + shift).wrap().color(color)
                component.append(pixel)
            }
            if (y != image.height - 1) {
                component.append(SpacingFontResources.spaced(-(image.width + 1)))
            }
        }
        return component
    }
}