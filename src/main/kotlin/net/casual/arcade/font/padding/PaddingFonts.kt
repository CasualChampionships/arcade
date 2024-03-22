package net.casual.arcade.font.padding

import net.casual.arcade.Arcade
import net.casual.arcade.font.IndexedBitmapFont
import net.minecraft.network.chat.MutableComponent

private val SPLIT_TEXTURE = Arcade.id("font/split.png")
private val NO_SPLIT_TEXTURE = Arcade.id("font/no_split.png")

private const val ASCENT = Short.MIN_VALUE.toInt()
private const val SIZE = 255

private val SIZE_RANGE = -SIZE..SIZE

public object PaddingSplitFont: IndexedBitmapFont(Arcade.id("padding_split")) {
    init {
        for (i in SIZE_RANGE) {
            indexed(SPLIT_TEXTURE, ASCENT, i)
        }
    }

    public fun padding(amount: Int): MutableComponent {
        require(amount in SIZE_RANGE) { "Cannot get padding outside of range $amount" }
        return this.get(amount)
    }
}

public object PaddingNoSplitFont: IndexedBitmapFont(Arcade.id("padding_no_split")) {
    init {
        for (i in SIZE_RANGE) {
            indexed(NO_SPLIT_TEXTURE, ASCENT, i)
        }
    }

    public fun padding(amount: Int): MutableComponent {
        require(amount in SIZE_RANGE) { "Cannot get padding outside of range $amount" }
        return this.get(amount)
    }
}
