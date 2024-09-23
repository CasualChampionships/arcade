package net.casual.arcade.resources.font.padding

import net.casual.arcade.resources.font.IndexedBitmapFontResources
import net.casual.arcade.utils.ResourceUtils
import net.minecraft.network.chat.MutableComponent

private val SPLIT_TEXTURE = ResourceUtils.arcade("font/split.png")
private val NO_SPLIT_TEXTURE = ResourceUtils.arcade("font/no_split.png")

private const val ASCENT = Short.MIN_VALUE.toInt()
private const val SIZE = 255

private val SIZE_RANGE = -SIZE..SIZE

public object PaddingSplitFontResources: IndexedBitmapFontResources(ResourceUtils.arcade("padding_split")) {
    init {
        for (i in SIZE_RANGE) {
            if (i != 0) {
                indexed(SPLIT_TEXTURE, ASCENT, i)
            }
        }
    }

    public fun padding(amount: Int): MutableComponent {
        return this.get(amount + SIZE - 2)
    }
}

public object PaddingNoSplitFontResources: IndexedBitmapFontResources(ResourceUtils.arcade("padding_no_split")) {
    init {
        for (i in SIZE_RANGE) {
            if (i != 0) {
                indexed(NO_SPLIT_TEXTURE, ASCENT, i)
            }
        }
    }

    public fun padding(amount: Int): MutableComponent {
        return this.get(amount + SIZE - 2)
    }
}
