package net.casual.arcade.chat

import net.casual.arcade.utils.ComponentUtils.colour
import net.casual.arcade.utils.ComponentUtils.lime
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.ComponentUtils.strikethrough
import net.minecraft.network.chat.Component

public fun interface ChatFormatter {
    public fun format(message: Component): Component

    public companion object {
        private val separator = Component.literal(" ".repeat(53)).strikethrough()

        public val SYSTEM: ChatFormatter = ChatFormatter {
            Component.empty().append("[\uD83D\uDCBB] ".literal().colour(0xA9A9A9)).append(it)
        }

        public val ANNOUNCEMENT: ChatFormatter = this.createAnnouncement()

        public fun createAnnouncement(title: Component? = null): ChatFormatter {
            return ChatFormatter {
                Component.empty().apply {
                    append(separator)
                    append("\n")
                    if (title != null) {
                        append(title)
                        append("\n")
                    }
                    append(it)
                    append("\n")
                    append(separator)
                }
            }
        }
    }
}