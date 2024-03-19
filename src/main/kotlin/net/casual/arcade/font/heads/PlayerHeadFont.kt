package net.casual.arcade.font.heads

import net.casual.arcade.Arcade
import net.casual.arcade.font.BitmapFont
import net.casual.arcade.utils.ComponentUtils
import net.minecraft.network.chat.MutableComponent

public object PlayerHeadFont: BitmapFont(Arcade.id("player_heads")) {
    private val PIXELS = Array(8, ::register)

    public val STEVE_HEAD: MutableComponent by add(texture("steve.png"), 8, 8)
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
        return add(texture("pixel${index}.png"), 8, 8)
    }
}