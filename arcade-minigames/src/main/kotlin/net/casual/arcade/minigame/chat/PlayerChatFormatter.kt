package net.casual.arcade.minigame.chat

import net.casual.arcade.utils.ComponentUtils.colour
import net.casual.arcade.utils.ComponentUtils.hover
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.ComponentUtils.red
import net.casual.arcade.utils.PlayerUtils.getChatPrefix
import net.minecraft.ChatFormatting.DARK_GRAY
import net.minecraft.ChatFormatting.WHITE
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.scores.PlayerTeam

public fun interface PlayerChatFormatter {
    public fun format(player: ServerPlayer, message: Component): PlayerFormattedChat

    public fun format(message: PlayerFormattedChat): PlayerFormattedChat {
        return message
    }

    public companion object {
        public val GLOBAL: PlayerChatFormatter = object: PlayerChatFormatter {
            private val globe by literal("[\uD83C\uDF10] ") {
                hover(Component.translatable("minigame.chat.mode.global"))
                colour(0xADD8E6)
            }

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

        public val SPECTATOR: PlayerChatFormatter = PlayerChatFormatter { player, message ->
            val icon = "[\uD83D\uDD76] ".literal().withStyle(DARK_GRAY)
                .hover(Component.translatable("minigame.chat.mode.spectator"))
            val prefix = Component.empty().append(icon)
            prefix.append(player.getChatPrefix(false))
            PlayerFormattedChat(message, prefix)
        }

        public val ADMIN: PlayerChatFormatter = PlayerChatFormatter { player, message ->
            val icon = "[\uD83D\uDC64] ".literal().red()
                .hover(Component.translatable("minigame.chat.mode.admin"))
            val prefix = Component.empty().append(icon)
            prefix.append(player.getChatPrefix(false))
            PlayerFormattedChat(message, prefix)
        }

        public val TEAM: PlayerChatFormatter = createTeamFormatter()

        public fun createTeamFormatter(supplier: (ServerPlayer) -> PlayerTeam? = ServerPlayer::getTeam): PlayerChatFormatter {
            return PlayerChatFormatter { player, message ->
                val team = supplier.invoke(player)
                val colour = if (team == null) WHITE else team.color
                val name = if (team == null) {
                    Component.translatable("minigame.chat.mode.team.unknown")
                } else {
                    Component.translatable("minigame.chat.mode.team", team.displayName)
                }
                val icon = "[⚐] ".literal().withStyle(colour)
                    .hover(name)
                val prefix = Component.empty().append(icon)
                prefix.append(player.getChatPrefix(false))
                PlayerFormattedChat(message, prefix)
            }
        }
    }
}