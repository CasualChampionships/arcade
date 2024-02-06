package net.casual.arcade.chat

import net.casual.arcade.utils.ComponentUtils.colour
import net.casual.arcade.utils.ComponentUtils.literal
import net.minecraft.network.chat.Component

public fun interface ChatFormatter {
    public fun format(message: Component): Component

    public companion object {
        public val SYSTEM: ChatFormatter = ChatFormatter {
            Component.empty().append("[\uD83D\uDCBB] ".literal().colour(0xA9A9A9)).append(it)
        }
    }
}