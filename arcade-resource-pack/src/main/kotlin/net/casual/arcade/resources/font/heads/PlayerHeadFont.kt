package net.casual.arcade.resources.font.heads

import net.casual.arcade.resources.font.FontResources
import net.casual.arcade.utils.ComponentUtils
import net.casual.arcade.utils.ResourceUtils
import net.minecraft.network.chat.MutableComponent

public object PlayerHeadFont: FontResources(ResourceUtils.arcade("player_heads")) {
    private val PIXELS = Array(8, PlayerHeadFont::register)

    public val STEVE_HEAD: MutableComponent by bitmap(at("steve.png"), 8, 8)
    public val PIXEL_0: MutableComponent by PIXELS[0]
    public val PIXEL_1: MutableComponent by PIXELS[1]
    public val PIXEL_2: MutableComponent by PIXELS[2]
    public val PIXEL_3: MutableComponent by PIXELS[3]
    public val PIXEL_4: MutableComponent by PIXELS[4]
    public val PIXEL_5: MutableComponent by PIXELS[5]
    public val PIXEL_6: MutableComponent by PIXELS[6]
    public val PIXEL_7: MutableComponent by PIXELS[7]

    public fun pixel(index: Int): MutableComponent {
        return PIXELS[index].generate()
    }

    private fun register(index: Int): ComponentUtils.ConstantComponentGenerator {
        return bitmap(at("pixel${index}.png"), 8, 8)
    }
}