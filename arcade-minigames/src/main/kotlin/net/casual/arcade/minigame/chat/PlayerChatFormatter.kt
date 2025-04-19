/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.chat

import com.mojang.serialization.Codec
import net.casual.arcade.minigame.utils.DisplayableTeam
import net.casual.arcade.minigame.utils.DisplayableTeam.Companion.displayable
import net.casual.arcade.minigame.utils.MinigameRegistries
import net.casual.arcade.utils.ComponentUtils.color
import net.casual.arcade.utils.ComponentUtils.hover
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.ComponentUtils.red
import net.casual.arcade.utils.PlayerUtils.getChatPrefix
import net.casual.arcade.utils.TeamUtils.color
import net.minecraft.ChatFormatting.DARK_GRAY
import net.minecraft.core.Registry
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.scores.PlayerTeam

public interface PlayerChatFormatter {
    public fun format(player: ServerPlayer, message: Component): PlayerFormattedChat

    public fun format(message: PlayerFormattedChat): PlayerFormattedChat {
        return message
    }

    public object Global: PlayerChatFormatter {
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

    public object Spectator: PlayerChatFormatter {
        override fun format(player: ServerPlayer, message: Component): PlayerFormattedChat {
            val icon = Component.literal("[\uD83D\uDD76] ").withStyle(DARK_GRAY)
                .hover(Component.translatable("minigame.chat.spectator"))
            val prefix = Component.empty().append(icon)
            prefix.append(player.getChatPrefix(false))
            return PlayerFormattedChat(message, prefix)
        }
    }

    public object Admin: PlayerChatFormatter {
        override fun format(player: ServerPlayer, message: Component): PlayerFormattedChat {
            val icon = Component.literal("[\uD83D\uDC64] ").red()
                .hover(Component.translatable("minigame.chat.admin"))
            val prefix = Component.empty().append(icon)
            prefix.append(player.getChatPrefix(false))
            return PlayerFormattedChat(message, prefix)
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
    override fun format(player: ServerPlayer, message: Component): PlayerFormattedChat {
        val team = this.teamGetter.invoke(player)
        val icon = Component.literal("[⚐] ")
        val name = if (team != null) {
            val name = Component.translatable("minigame.chat.team", team.name)
            if (team.color != null) {
                icon.color(team.color)
                name.color(team.color)
            }
            name
        } else {
            Component.translatable("minigame.chat.team.unknown")
        }
        icon.hover(name)
        val prefix = Component.empty().append(icon)
        prefix.append(player.getChatPrefix(false))
        return PlayerFormattedChat(message, prefix)
    }
}