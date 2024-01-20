package net.casual.arcade.minigame.managers

import net.casual.arcade.events.player.PlayerChatEvent
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.annotation.NONE
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.PlayerUtils.getChatPrefix
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
        if (this.minigame.settings.isTeamChat) {
            val team = event.player.team
            if (team == null || this.minigame.teams.isTeamIgnored(team)) {
                return
            }

            val content = event.rawMessage
            if (content.startsWith("!")) {
                val decorated = content.substring(1)
                if (decorated.isNotBlank()) {
                    event.replaceMessage(decorated.trim().literal())
                }
                return
            }
            val prefix = team.formattedDisplayName.append(" ").append(event.player.getChatPrefix())
            event.replaceMessage(event.message.decoratedContent(), prefix)
            event.addFilter { team == it.team }
        }
    }
}