package net.casual.arcade.resources.font.heads

import net.casual.arcade.resources.font.FontResources
import net.casual.arcade.utils.ResourceUtils
import net.minecraft.network.chat.Component

public object PlayerHeadFont: FontResources(ResourceUtils.arcade("player_heads")) {
    private val PIXELS = Array(8, PlayerHeadFont::register)

    public val STEVE_HEAD: Component = bitmap(at("steve.png"), 8, 8)

    public fun pixel(index: Int): Component {
        return PIXELS[index]
    }

    private fun register(index: Int): Component {
        return bitmap(at("pixel${index}.png"), 8, 8)
    }
}