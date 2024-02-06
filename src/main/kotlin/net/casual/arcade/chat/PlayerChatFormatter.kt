package net.casual.arcade.chat

import net.casual.arcade.utils.ComponentUtils.colour
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.ComponentUtils.red
import net.casual.arcade.utils.PlayerUtils.getChatPrefix
import net.minecraft.ChatFormatting.*
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

public fun interface PlayerChatFormatter {
    public fun format(player: ServerPlayer, message: Component): PlayerFormattedChat

    public companion object {
        public val GLOBAL: PlayerChatFormatter = PlayerChatFormatter { player, message ->
            val prefix = Component.empty().append("[\uD83C\uDF10] ".literal().colour(0xADD8E6))
            val team = player.team
            if (team != null) {
                prefix.append(team.formattedDisplayName).append(" ")
            }
            prefix.append(player.getChatPrefix(false))
            PlayerFormattedChat(message, prefix)
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
            val prefix = "[".literal().append("✩".literal().red()).append("] ")
            prefix.append(player.getChatPrefix(false))
            PlayerFormattedChat(message, prefix)
        }
    }
}