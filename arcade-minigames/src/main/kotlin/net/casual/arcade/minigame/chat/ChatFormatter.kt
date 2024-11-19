package net.casual.arcade.minigame.chat

import net.casual.arcade.utils.ComponentUtils.color
import net.casual.arcade.utils.ComponentUtils.strikethrough
import net.minecraft.network.chat.Component

/**
 * This interface is used to format chat messages.
 */
public fun interface ChatFormatter {
    /**
     * This method formats the given [message] and returns the formatted [Component].
     *
     * @param message The message to format.
     * @return The formatted message.
     */
    public fun format(message: Component): Component

    public companion object {
        private val separator = Component.literal(" ".repeat(80)).strikethrough()

        /**
         * This is the system default chat formatter.
         * It prefixes the given message with [💻].
         */
        public val SYSTEM: ChatFormatter = ChatFormatter {
            Component.empty().append(Component.literal("[\uD83D\uDCBB] ").color(0xA9A9A9)).append(it)
        }

        /**
         * This is the announcement chat formatter.
         * It adds a strikethrough to the line above and below the message.
         */
        public val ANNOUNCEMENT: ChatFormatter = createAnnouncement()

        /**
         * This method creates an announcement chat formatter.
         * It adds a strikethrough to the line above and below the message
         * as well as adding a title to the announcement.
         * For example:
         * ```
         * --------------------------------------------------------------
         * [Title]
         * Lorem ipsum dolor sit amet, consectetur adipiscing elit.
         * --------------------------------------------------------------
         * ```
         *
         * @param title The title of the announcement.
         * @return The announcement chat formatter.
         */
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