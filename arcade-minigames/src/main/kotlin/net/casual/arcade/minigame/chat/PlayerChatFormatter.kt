package net.casual.arcade.minigame.chat

import net.casual.arcade.utils.ComponentUtils.color
import net.casual.arcade.utils.ComponentUtils.hover
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.ComponentUtils.red
import net.casual.arcade.utils.PlayerUtils.getChatPrefix
import net.casual.arcade.utils.TeamUtils.color
import net.minecraft.ChatFormatting.DARK_GRAY
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.scores.PlayerTeam

public interface PlayerChatFormatter {
    public fun format(player: ServerPlayer, message: Component): PlayerFormattedChat

    public fun format(message: PlayerFormattedChat): PlayerFormattedChat {
        return message
    }

    public companion object {
        public val GLOBAL: PlayerChatFormatter = object: PlayerChatFormatter {
            private val globe by literal("[\uD83C\uDF10] ") {
                hover(Component.translatable("minigame.chat.global"))
                color(0xADD8E6)
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

        public val SPECTATOR: PlayerChatFormatter = object: PlayerChatFormatter {
            override fun format(player: ServerPlayer, message: Component): PlayerFormattedChat {
                val icon = "[\uD83D\uDD76] ".literal().withStyle(DARK_GRAY)
                    .hover(Component.translatable("minigame.chat.spectator"))
                val prefix = Component.empty().append(icon)
                prefix.append(player.getChatPrefix(false))
                return PlayerFormattedChat(message, prefix)
            }
        }

        public val ADMIN: PlayerChatFormatter = object: PlayerChatFormatter {
            override fun format(player: ServerPlayer, message: Component): PlayerFormattedChat {
                val icon = "[\uD83D\uDC64] ".literal().red()
                    .hover(Component.translatable("minigame.chat.admin"))
                val prefix = Component.empty().append(icon)
                prefix.append(player.getChatPrefix(false))
                return PlayerFormattedChat(message, prefix)
            }
        }

        public val TEAM: PlayerChatFormatter = createTeamFormatter()

        public fun createTeamFormatter(supplier: (ServerPlayer) -> PlayerTeam? = ServerPlayer::getTeam): PlayerChatFormatter {
            return TeamChatFormatter(supplier)
        }
    }
}

private class TeamChatFormatter(private val teamGetter: (ServerPlayer) -> PlayerTeam?): PlayerChatFormatter {
    override fun format(player: ServerPlayer, message: Component): PlayerFormattedChat {
        val team = this.teamGetter.invoke(player)
        val icon = "[⚐] ".literal()
        val name = if (team != null) {
            icon.color(team)
            Component.translatable("minigame.chat.team", team.displayName).color(team)
        } else {
            Component.translatable("minigame.chat.team.unknown")
        }
        icon.hover(name)
        val prefix = Component.empty().append(icon)
        prefix.append(player.getChatPrefix(false))
        return PlayerFormattedChat(message, prefix)
    }
}