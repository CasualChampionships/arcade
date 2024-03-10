package net.casual.arcade.chat

import net.casual.arcade.utils.ComponentUtils.colour
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.ComponentUtils.red
import net.casual.arcade.utils.PlayerUtils.getChatPrefix
import net.minecraft.ChatFormatting.DARK_GRAY
import net.minecraft.ChatFormatting.WHITE
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

public fun interface PlayerChatFormatter {
    public fun format(player: ServerPlayer, message: Component): PlayerFormattedChat

    public fun format(message: PlayerFormattedChat): PlayerFormattedChat {
        return message
    }

    public companion object {
        public val GLOBAL: PlayerChatFormatter = object: PlayerChatFormatter {
            private val globe by literal("[\uD83C\uDF10] ") { colour(0xADD8E6) }

            override fun format(player: ServerPlayer, message: Component): PlayerFormattedChat {
                val prefix = Component.empty().append(this.globe)
                val team = player.team
                if (team != null) {
                    prefix.append(team.formattedDisplayName).append(" ")
                }
                prefix.append(player.getChatPrefix(false))
                return PlayerFormattedChat(message, prefix)
            }

            override fun format(message: PlayerFormattedChat): PlayerFormattedChat {
                val prefix = Component.empty().append(this.globe).append(message.prefix ?: Component.empty())
                return PlayerFormattedChat(message.message, prefix)
            }
        }

        public val TEAM: PlayerChatFormatter = PlayerChatFormatter { player, message ->
            val team = player.team
            val colour = if (team == null) WHITE else team.color
            val prefix = Component.empty().append("[⚐] ".literal().withStyle(colour))
            prefix.append(player.getChatPrefix(false))
            PlayerFormattedChat(message, prefix)
        }

        public val SPECTATOR: PlayerChatFormatter = PlayerChatFormatter { player, message ->
            val prefix = Component.empty().append("[\uD83D\uDD76] ".literal().withStyle(DARK_GRAY))
            prefix.append(player.getChatPrefix(false))
            PlayerFormattedChat(message, prefix)
        }

        public val ADMIN: PlayerChatFormatter = PlayerChatFormatter { player, message ->
            val prefix = Component.empty().append("[\uD83D\uDC64] ".literal().red())
            prefix.append(player.getChatPrefix(false))
            PlayerFormattedChat(message, prefix)
        }
    }
}