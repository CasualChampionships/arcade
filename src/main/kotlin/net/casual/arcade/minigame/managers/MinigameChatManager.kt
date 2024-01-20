package net.casual.arcade.minigame.managers

import net.casual.arcade.events.player.PlayerChatEvent
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.annotation.NONE
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.ComponentUtils.red
import net.casual.arcade.utils.PlayerUtils.getChatPrefix
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

public class MinigameChatManager(
    private val minigame: Minigame<*>
) {
    init {
        this.minigame.events.register<PlayerChatEvent>(1_000, NONE, this::onGlobalPlayerChat)
        this.minigame.events.register<PlayerChatEvent> { this.onPlayerChat(it) }
    }

    public fun isMessageGlobal(sender: ServerPlayer, message: String): Boolean {
        val team = sender.team
        return team == null || this.minigame.teams.isTeamIgnored(team) || message.startsWith("!")
    }

    private fun onGlobalPlayerChat(event: PlayerChatEvent) {
        if (!this.minigame.settings.isChatGlobal && !this.minigame.hasPlayer(event.player)) {
            event.addFilter { !this.minigame.hasPlayer(it) }
        }
    }

    private fun onPlayerChat(event: PlayerChatEvent) {
        val (player, message) = event
        if (this.minigame.settings.isChatMuted.get(player)) {
            event.cancel()
            player.sendSystemMessage("Currently chat is muted".literal().red())
            return
        }

        if (this.minigame.settings.isTeamChat) {
            val team = player.team
            if (team == null || this.minigame.teams.isTeamIgnored(team)) {
                return
            }

            val content = event.rawMessage
            if (content.startsWith("!")) {
                val decorated = content.substring(1)
                if (decorated.isNotBlank()) {
                    event.replaceMessage(decorated.trim().literal())
                } else {
                    event.cancel()
                }
                return
            }
            val prefix = Component.empty().append(team.formattedDisplayName).append(" ").append(player.getChatPrefix(false))
            event.replaceMessage(message.decoratedContent(), prefix)
            event.addFilter { team == it.team }
        }
    }
}