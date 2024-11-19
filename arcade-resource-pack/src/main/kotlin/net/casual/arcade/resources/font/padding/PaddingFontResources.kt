package net.casual.arcade.resources.font.padding

import net.casual.arcade.resources.font.IndexedFontResources
import net.casual.arcade.utils.ResourceUtils
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

private val SPLIT_TEXTURE = ResourceUtils.arcade("font/split.png")
private val NO_SPLIT_TEXTURE = ResourceUtils.arcade("font/no_split.png")

private const val ASCENT = Short.MIN_VALUE.toInt()
private const val SIZE = 255

private val SIZE_RANGE = -SIZE..SIZE

public object PaddingSplitFontResources: IndexedFontResources(ResourceUtils.arcade("padding_split")) {
    init {
        for (i in SIZE_RANGE) {
            if (i != 0) {
                this.indexed { this.bitmap(SPLIT_TEXTURE, ASCENT, i) }
            }
        }
    }

    public fun padding(amount: Int): Component {
        require(amount in SIZE_RANGE) { "Invalid amount of padding: $amount, must be in range $SIZE_RANGE" }
        return this.get(amount + SIZE - 2)
    }
}

public object PaddingNoSplitFontResources: IndexedFontResources(ResourceUtils.arcade("padding_no_split")) {
    init {
        for (i in SIZE_RANGE) {
            if (i != 0) {
                this.indexed { this.bitmap(NO_SPLIT_TEXTURE, ASCENT, i) }
            }
        }
    }

    public fun padding(amount: Int): Component {
        require(amount in SIZE_RANGE) { "Invalid amount of padding: $amount, must be in range $SIZE_RANGE" }
        return this.get(amount + SIZE - 2)
    }
}
