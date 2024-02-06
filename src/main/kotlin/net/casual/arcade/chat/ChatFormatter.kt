package net.casual.arcade.chat

import net.casual.arcade.utils.ComponentUtils.colour
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.PlayerUtils.getChatPrefix
import net.minecraft.ChatFormatting
import net.minecraft.ChatFormatting.WHITE
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

public fun interface ChatFormatter {
    public fun format(player: ServerPlayer, message: Component): FormattedChat

    public companion object {
        public val GLOBAL: ChatFormatter = ChatFormatter { player, message ->
            val prefix = Component.empty().append("[\uD83C\uDF10] ".literal().colour(0xADD8E6))
            val team = player.team
            if (team != null) {
                prefix.append(team.formattedDisplayName).append(" ")
            }
            prefix.append(player.getChatPrefix(false))
            FormattedChat(message, prefix)
        }

        public val TEAM: ChatFormatter = ChatFormatter { player, message ->
            val team = player.team
            val (name, colour) = if (team == null) null to WHITE else team.formattedDisplayName to team.color
            val prefix = Component.empty().append("[\uD83C\uDFF3] ".literal().withStyle(colour))
            if (name != null) {
                prefix.append(name).append(" ")
            }
            prefix.append(player.getChatPrefix(false))
            FormattedChat(message, prefix)
        }
    }
}