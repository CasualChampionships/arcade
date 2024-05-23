package net.casual.arcade.minigame.managers

import net.casual.arcade.Arcade
import net.casual.arcade.chat.ChatFormatter
import net.casual.arcade.chat.PlayerChatFormatter
import net.casual.arcade.chat.PlayerFormattedChat
import net.casual.arcade.events.BuiltInEventPhases.DEFAULT
import net.casual.arcade.events.player.PlayerChatEvent
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.MinigameSettings
import net.casual.arcade.minigame.annotation.ListenerFlags
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.ComponentUtils.red
import net.casual.arcade.utils.PlayerUtils.getChatPrefix
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import java.util.*

/**
 * This class manages the chat of a minigame.
 * This class lets you:
 * - Separate chat into global, team, admin, and spectator chat.
 * - Specify the formatting for player chat messages, depending on the chat.
 * - Mute chat for all players.
 * - Add and remove chat spies (to let anyone view all chats).
 * - Broadcast messages to players with a [ChatFormatter].
 *
 * @see Minigame.chat
 */
public class MinigameChatManager(
    private val minigame: Minigame<*>
) {
    /**
     * The formatter for global player chat messages.
     *
     * To specify whether the chat is global, use [MinigameSettings.isChatGlobal].
     */
    public var globalChatFormatter: PlayerChatFormatter = PlayerChatFormatter.GLOBAL

    /**
     * The formatter for team player chat messages.
     */
    public var teamChatFormatter: PlayerChatFormatter = PlayerChatFormatter.TEAM

    /**
     * The formatter for admin player chat messages.
     */
    public var adminChatFormatter: PlayerChatFormatter = PlayerChatFormatter.ADMIN

    /**
     * The formatter for spectator player chat messages.
     */
    public var spectatorChatFormatter: PlayerChatFormatter = PlayerChatFormatter.SPECTATOR

    /**
     * The formatter for system chat messages.
     */
    public var systemChatFormatter: ChatFormatter? = null

    /**
     * The message to broadcast to a player when chat is muted.
     *
     * To specify whether the chat is muted, use [MinigameSettings.isChatMuted].
     */
    public var mutedMessage: Component = "Currently chat is muted".literal().red()

    internal val spies = HashSet<UUID>()

    init {
        this.minigame.events.register<PlayerChatEvent>(1_000, DEFAULT, ListenerFlags.NONE, this::onGlobalPlayerChat)
        this.minigame.events.register<PlayerChatEvent> { this.onPlayerChat(it) }
    }

    /**
     * Broadcasts a message to all players in the minigame with the specified [ChatFormatter].
     *
     * @param message The message to broadcast.
     * @param formatter The formatter to format the message with, [systemChatFormatter] by default.
     */
    public fun broadcast(message: Component, formatter: ChatFormatter? = this.systemChatFormatter) {
        this.broadcastTo(message, this.getAllPlayers(), formatter)
    }

    /**
     * Broadcasts a message to the specified players with the specified [ChatFormatter].
     *
     * @param message The message to broadcast.
     * @param players The players to broadcast the message to.
     * @param formatter The formatter to format the message with, [systemChatFormatter] by default.
     */
    public fun broadcastTo(
        message: Component,
        players: Iterable<ServerPlayer>,
        formatter: ChatFormatter? = this.systemChatFormatter
    ) {
        val formatted = formatter?.format(message) ?: message
        for (player in players) {
            player.sendSystemMessage(formatted)
        }
    }

    /**
     * Broadcasts a message to a player with the specified [ChatFormatter].
     *
     * @param message The message to broadcast.
     * @param player The player to broadcast the message to.
     * @param formatter The formatter to format the message with, [systemChatFormatter] by default.
     */
    public fun broadcastTo(
        message: Component,
        player: ServerPlayer,
        formatter: ChatFormatter? = this.systemChatFormatter
    ) {
        val formatted = formatter?.format(message) ?: message
        player.sendSystemMessage(formatted)
    }

    /**
     * Broadcasts a message from a player to a specific player with the specified [PlayerChatFormatter].
     *
     * @param player The player who sent the message.
     * @param message The message to broadcast.
     * @param receiver The player to broadcast the message to.
     * @param formatter The formatter to format the message with.
     */
    public fun broadcastAsPlayerTo(
        player: ServerPlayer,
        message: Component,
        receiver: ServerPlayer,
        formatter: PlayerChatFormatter
    ) {
        val (decorated, prefix) = formatter.format(player, message)
        val newPrefix = prefix ?: player.getChatPrefix(true)
        val chat = Component.empty().append(newPrefix).append(decorated)
        receiver.sendSystemMessage(chat)
    }

    /**
     * Broadcasts a message from a player to a collection of players with the specified [PlayerChatFormatter].
     *
     * @param player The player who sent the message.
     * @param message The message to broadcast.
     * @param receivers The players to broadcast the message to.
     * @param formatter The formatter to format the message with.
     */
    public fun broadcastAsPlayerTo(
        player: ServerPlayer,
        message: Component,
        receivers: Collection<ServerPlayer>,
        formatter: PlayerChatFormatter
    ) {
        val (decorated, prefix) = formatter.format(player, message)
        val newPrefix = prefix ?: player.getChatPrefix(true)
        val chat = Component.empty().append(newPrefix).append(decorated)
        for (receiver in receivers) {
            receiver.sendSystemMessage(chat)
        }
    }

    /**
     * Determines whether a message from the [sender] should be sent to global chat.
     *
     * @param sender The player who sent the message.
     * @param message The message to check.
     * @return Whether the message should be sent to global chat.
     */
    public fun isMessageGlobal(sender: ServerPlayer, message: String): Boolean {
        val team = sender.team
        return team == null || this.minigame.teams.isTeamIgnored(team) || message.startsWith("!")
    }

    /**
     * This adds a player to the list of chat spies.
     *
     * This will mean the player will be able to see all chat messages
     * from all chats.
     *
     * @param player The player to add as a chat spy.
     */
    public fun addSpy(player: ServerPlayer) {
        this.spies.add(player.uuid)
    }

    /**
     * This removes a player from the list of chat spies.
     *
     * @param player The player to remove as a chat spy.
     */
    public fun removeSpy(player: ServerPlayer) {
        this.spies.remove(player.uuid)
    }

    /**
     * Determines whether a player is a chat spy.
     *
     * @param player The player to check.
     * @return Whether the player is a chat spy.
     */
    public fun isSpy(player: ServerPlayer): Boolean {
        return this.spies.contains(player.uuid)
    }

    /**
     * Mutes a player from talking in chat.
     *
     * @param player The player to mute.
     * @return Whether the mute was successful.
     */
    public fun mute(player: ServerPlayer): Boolean {
        return this.minigame.tags.add(player, MUTED)
    }

    /**
     * Checks whether a player is muted.
     *
     * @param player The player to check.
     * @return Whether they are muted.
     */
    public fun isMuted(player: ServerPlayer): Boolean {
        return this.minigame.tags.has(player, MUTED)
    }

    /**
     * Unmutes a player from talking in chat.
     *
     * @param player The player to unmute.
     * @return Whether unmuting was successful.
     */
    public fun unmute(player: ServerPlayer): Boolean {
        return this.minigame.tags.remove(player, MUTED)
    }

    /**
     * Gets all players in the minigame.
     *
     * @return The list of players.
     */
    public fun getAllPlayers(): Iterable<ServerPlayer> {
        return this.minigame.players
    }

    private fun onGlobalPlayerChat(event: PlayerChatEvent) {
        if (!this.minigame.settings.canCrossChat && !this.minigame.players.has(event.player)) {
            event.addFilter { !this.minigame.players.has(it) }
        }
    }

    private fun onPlayerChat(event: PlayerChatEvent) {
        val (player, message) = event
        if (this.minigame.settings.isChatMuted.get(player)) {
            event.cancel()
            this.broadcastTo(this.mutedMessage, player)
            return
        }

        if (this.minigame.settings.isChatGlobal) {
            if (!this.minigame.settings.useVanillaChat) {
                val (decorated, prefix) = this.formatGlobalChatFor(player, message.decoratedContent())
                event.replaceMessage(decorated, prefix)
            }
            return
        }

        val team = player.team

        val content = event.rawMessage
        val exclaimed = content.startsWith("!")
        if (exclaimed || team == null || this.minigame.teams.isTeamEliminated(team)) {
            val trimmed = if (exclaimed) content.substring(1) else content
            if (trimmed.isNotBlank()) {
                val (decorated, prefix) = this.formatGlobalChatFor(player, trimmed.trim().literal())
                event.replaceMessage(decorated, prefix)
            } else {
                event.cancel()
            }
            return
        }

        if (this.minigame.players.isAdmin(player)) {
            val (decorated, prefix) = this.adminChatFormatter.format(player, message.decoratedContent())
            event.replaceMessage(decorated, prefix)
            event.addFilter { this.minigame.players.isAdmin(it) || this.isSpy(it) }
            return
        }

        if (this.minigame.players.isSpectating(player)) {
            val (decorated, prefix) = this.spectatorChatFormatter.format(player, message.decoratedContent())
            event.replaceMessage(decorated, prefix)
            event.addFilter { this.minigame.players.isSpectating(it) || this.isSpy(it) }
            return
        }

        val (decorated, prefix) = this.teamChatFormatter.format(player, message.decoratedContent())
        event.replaceMessage(decorated, prefix)
        event.addFilter { team == it.team || this.isSpy(it) }
        return
    }

    private fun formatGlobalChatFor(player: ServerPlayer, message: Component): PlayerFormattedChat {
        if (this.minigame.players.isAdmin(player)) {
            return this.globalChatFormatter.format(this.adminChatFormatter.format(player, message))
        }
        if (this.minigame.players.isSpectating(player)) {
            return this.globalChatFormatter.format(this.spectatorChatFormatter.format(player, message))
        }
        return this.globalChatFormatter.format(player, message)
    }

    public companion object {
        public val MUTED: ResourceLocation = Arcade.id("muted")
    }
}