/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.chat

import net.casual.arcade.utils.ComponentUtils.color
import net.casual.arcade.utils.ComponentUtils.hover
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.ComponentUtils.red
import net.casual.arcade.utils.ComponentUtils.wrap
import net.casual.arcade.utils.PlayerUtils.getChatUsername
import net.casual.arcade.utils.team.DisplayableTeam
import net.casual.arcade.utils.team.DisplayableTeam.Companion.displayable
import net.minecraft.ChatFormatting.DARK_GRAY
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.scores.PlayerTeam

public interface PlayerChatFormatter {
    public fun format(player: ServerPlayer, message: PlayerFormattedChat): PlayerFormattedChat

    public object None: PlayerChatFormatter {
        override fun format(player: ServerPlayer, message: PlayerFormattedChat): PlayerFormattedChat {
            return message
        }
    }

    public object Global: PlayerChatFormatter {
        private val globe by literal("[\uD83C\uDF10] ") {
            hover(Component.translatable("arcade.chat.global"))
            color(0xADD8E6)
        }

        override fun format(player: ServerPlayer, message: PlayerFormattedChat): PlayerFormattedChat {
            val prefix = Component.empty().append(this.globe).append(message.prefix)
            return message.copy(prefix = prefix)
        }
    }

    public object Spectator: PlayerChatFormatter {
        override fun format(player: ServerPlayer, message: PlayerFormattedChat): PlayerFormattedChat {
            val icon = Component.literal("[\uD83D\uDD76] ").withStyle(DARK_GRAY)
                .hover(Component.translatable("arcade.chat.spectator"))
            return message.copy(prefix = icon.wrap().append(message.prefix))
        }
    }

    public object Admin: PlayerChatFormatter {
        override fun format(player: ServerPlayer, message: PlayerFormattedChat): PlayerFormattedChat {
            val icon = Component.literal("[\uD83D\uDC64] ").red()
                .hover(Component.translatable("arcade.chat.admin"))
            return message.copy(prefix = icon.wrap().append(message.prefix))
        }
    }

    public object Team: PlayerChatFormatter by createTeamFormatter()

    public companion object {
        public fun createTeamFormatter(supplier: (ServerPlayer) -> PlayerTeam? = ServerPlayer::getTeam): PlayerChatFormatter {
            return TeamChatFormatter { supplier.invoke(it)?.displayable() }
        }

        public fun createTeamFormatter(team: PlayerTeam): PlayerChatFormatter {
            return TeamChatFormatter { team.displayable() }
        }

        public fun createTeamFormatter(displayable: DisplayableTeam): PlayerChatFormatter {
            return TeamChatFormatter { displayable }
        }
    }
}

private class TeamChatFormatter(private val teamGetter: (ServerPlayer) -> DisplayableTeam?): PlayerChatFormatter {
    override fun format(player: ServerPlayer, message: PlayerFormattedChat): PlayerFormattedChat {
        val team = this.teamGetter.invoke(player)
        val icon = Component.literal("[⚐] ")
        val name = if (team != null) {
            val name = Component.translatable("arcade.chat.team", team.name)
            if (team.color != null) {
                icon.color(team.color)
                name.color(team.color)
            }
            name
        } else {
            Component.translatable("arcade.chat.team.unknown")
        }
        icon.hover(name)
        return message.copy(
            prefix = icon.wrap().append(message.prefix),
            username = message.username ?: player.getChatUsername(false)
        )
    }
}