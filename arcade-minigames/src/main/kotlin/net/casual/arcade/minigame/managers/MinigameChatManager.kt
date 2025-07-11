/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.managers

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import com.mojang.serialization.JsonOps
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.casual.arcade.commands.CommandTree
import net.casual.arcade.commands.argument
import net.casual.arcade.commands.literal
import net.casual.arcade.events.BuiltInEventPhases.DEFAULT
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.events.server.player.PlayerChatEvent
import net.casual.arcade.events.server.player.PlayerSystemMessageEvent
import net.casual.arcade.events.server.player.PlayerTeamChatEvent
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.annotation.ListenerFlags
import net.casual.arcade.minigame.events.MinigameAddNewPlayerEvent
import net.casual.arcade.minigame.events.MinigameRemoveAdminEvent
import net.casual.arcade.minigame.events.MinigameSetPlayingEvent
import net.casual.arcade.minigame.events.MinigameSetSpectatingEvent
import net.casual.arcade.minigame.managers.chat.MinigameChatMode
import net.casual.arcade.minigame.settings.MinigameSettings
import net.casual.arcade.minigame.utils.MinigameUtils.isMinigameAdminOrHasPermission
import net.casual.arcade.minigame.utils.MinigameUtils.isPlayerAnd
import net.casual.arcade.minigame.utils.MinigameUtils.requiresAdminOrPermission
import net.casual.arcade.scheduler.GlobalTickedScheduler
import net.casual.arcade.utils.ComponentUtils.red
import net.casual.arcade.utils.JsonUtils.arrayOrDefault
import net.casual.arcade.utils.JsonUtils.obj
import net.casual.arcade.utils.JsonUtils.objects
import net.casual.arcade.utils.JsonUtils.uuid
import net.casual.arcade.utils.JsonUtils.uuids
import net.casual.arcade.utils.PlayerUtils.getChatUsername
import net.casual.arcade.utils.PlayerUtils.player
import net.casual.arcade.utils.ResourceUtils
import net.casual.arcade.utils.chat.ChatFormatter
import net.casual.arcade.utils.chat.PlayerChatFormatter
import net.casual.arcade.utils.chat.PlayerFormattedChat
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.arguments.TeamArgument
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
    private val minigame: Minigame
) {
    /**
     * The formatter for global player chat messages.
     *
     * To specify whether the chat is global, use [MinigameSettings.isChatGlobal].
     */
    public var globalChatFormatter: PlayerChatFormatter = PlayerChatFormatter.Global

    /**
     * The formatter for team player chat messages.
     */
    public var teamChatFormatter: PlayerChatFormatter = PlayerChatFormatter.Team

    /**
     * The formatter for admin player chat messages.
     */
    public var adminChatFormatter: PlayerChatFormatter = PlayerChatFormatter.Admin

    /**
     * The formatter for spectator player chat messages.
     */
    public var spectatorChatFormatter: PlayerChatFormatter = PlayerChatFormatter.Spectator

    /**
     * The formatter for system chat messages.
     */
    public var systemChatFormatter: ChatFormatter? = null

    /**
     * The message to broadcast to a player when chat is muted.
     *
     * To specify whether the chat is muted, use [MinigameSettings.isChatMuted].
     */
    public var mutedMessage: Component = Component.translatable("minigame.chat.muted").red()

    internal val modes = Object2ObjectOpenHashMap<UUID, MinigameChatMode>()
    internal val spies = ObjectOpenHashSet<UUID>()

    init {
        this.minigame.events.register<PlayerChatEvent>(1_000, DEFAULT, ListenerFlags.NONE, listener = this::onGlobalPlayerChat)
        this.minigame.events.register<PlayerSystemMessageEvent>(this::onGlobalSystemChat)
        this.minigame.events.register<PlayerChatEvent>(this::onPlayerChat)
        this.minigame.events.register<PlayerTeamChatEvent>(this::onPlayerTeamChat)
        this.minigame.events.register<MinigameSetPlayingEvent> { (_, player) ->
            if (!this.modes.containsKey(player.uuid)) {
                this.setChatModeFor(player, MinigameChatMode.OwnTeam)
            }
        }
        this.minigame.events.register<MinigameSetSpectatingEvent> { (_, player) ->
            if (!this.modes.containsKey(player.uuid)) {
                this.setChatModeFor(player, MinigameChatMode.Spectator)
            }
        }
        this.minigame.events.register<MinigameRemoveAdminEvent> { (_, player) ->
            if (this.modes[player.uuid] == MinigameChatMode.Admin) {
                this.setChatModeFor(player, MinigameChatMode.OwnTeam)
            }
        }
        this.minigame.events.register<MinigameAddNewPlayerEvent> { (_, player) ->
            GlobalTickedScheduler.later {
                if (!this.modes.containsKey(player.uuid) && !this.minigame.settings.isChatGlobal) {
                    this.broadcastTo(MinigameChatMode.OwnTeam.switchedToMessage(player), player)
                }
            }
        }

        this.modes.defaultReturnValue(MinigameChatMode.OwnTeam)

        this.registerCommand()
    }

    /**
     * Broadcasts a message to all players in the minigame with the specified [ChatFormatter].
     *
     * @param message The message to broadcast.
     * @param formatter The formatter to format the message with, [systemChatFormatter] by default.
     */
    public fun broadcast(message: Component, formatter: ChatFormatter? = this.systemChatFormatter, log: Boolean = false) {
        this.broadcastTo(message, this.getAllPlayers(), formatter, log)
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
        formatter: ChatFormatter? = this.systemChatFormatter,
        log: Boolean = false
    ) {
        val formatted = formatter?.format(message) ?: message
        for (player in players) {
            player.sendSystemMessage(formatted)
        }
        if (log) {
            this.minigame.server.sendSystemMessage(formatted)
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
        val formatted = formatter.format(player, PlayerFormattedChat(message = message))
        receiver.sendSystemMessage(formatted.asComponent { player.getChatUsername(true) })
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
        formatter: PlayerChatFormatter,
        log: Boolean = false
    ) {
        val formatted = formatter.format(player, PlayerFormattedChat(message = message))
        val chat = formatted.asComponent { player.getChatUsername(true) }
        for (receiver in receivers) {
            receiver.sendSystemMessage(chat)
        }
        if (log) {
            this.minigame.server.sendSystemMessage(chat)
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

    public fun setChatModeFor(
        player: ServerPlayer,
        mode: MinigameChatMode,
        feedback: Boolean = true
    ) {
        if (this.modes.put(player.uuid, mode) != mode) {
            if (!this.minigame.settings.isChatGlobal && feedback) {
                this.broadcastTo(mode.switchedToMessage(player), player)
            }
        } else if (!this.minigame.settings.isChatGlobal && feedback) {
            this.broadcastTo(Component.translatable("minigame.chat.mode.switch.alreadySelected"), player)
        }
    }

    public fun setChatModeFor(
        uuid: UUID,
        mode: MinigameChatMode,
        feedback: Boolean = true
    ) {
        val player = this.minigame.server.player(uuid)
        if (player != null) {
            this.setChatModeFor(player, mode, feedback)
            return
        }
        this.modes[uuid] = mode
    }

    public fun getChatModeFor(player: ServerPlayer): MinigameChatMode {
        return this.modes[player.uuid] ?: MinigameChatMode.Global
    }

    private fun onGlobalPlayerChat(event: PlayerChatEvent) {
        if (!this.minigame.settings.canCrossChat && !this.minigame.players.has(event.player)) {
            event.addFilter { !this.minigame.players.has(it) }
        }
    }

    private fun onGlobalSystemChat(event: PlayerSystemMessageEvent) {
        if (this.minigame.settings.isChatMuted.get() && !event.isActionBar) {
            event.cancel()
            return
        }

        val causer = event.causer
        if (causer != null) {
            if (!this.minigame.settings.canCrossChat && !this.minigame.players.has(causer.uuid)) {
                event.cancel()
                return
            }
        }

        val formatter = this.systemChatFormatter
        if (!event.isActionBar && this.minigame.settings.formatGlobalSystemChat && formatter != null) {
            event.message = formatter.format(event.message)
        }
    }

    private fun onPlayerChat(event: PlayerChatEvent) {
        val (player) = event
        if (this.minigame.settings.isChatMuted.get(player)) {
            event.cancel()
            this.broadcastTo(this.mutedMessage, player)
            return
        }

        if (this.minigame.settings.isChatGlobal) {
            if (!this.minigame.settings.useVanillaChat) {
                event.format { this.formatGlobalChatFor(player, it) }
            }
            return
        }

        val mode = this.modes[player.uuid]

        val content = event.rawMessage
        val exclaimed = content.startsWith("!")
        if (exclaimed || mode == null || mode == MinigameChatMode.Global) {
            val trimmed = if (exclaimed) content.substring(1) else content
            if (trimmed.isNotBlank()) {
                event.format { this.formatGlobalChatFor(player, it.copy(message = Component.literal(trimmed.trim()))) }
                event.addFilter { MinigameChatMode.Global.canSendTo(player, it, null, this.minigame) }
            } else {
                event.cancel()
            }
            return
        }

        val formatter = mode.getChatFormatter(this)
        event.format { formatter.format(player, it) }
        event.addFilter { this.isSpy(it) || mode.canSendTo(player, it, this.modes[it.uuid], this.minigame) }
    }

    private fun onPlayerTeamChat(event: PlayerTeamChatEvent) {
        val formatter = MinigameChatMode.OwnTeam.getChatFormatter(this)
        event.format { formatter.format(event.player, it) }

        for (spy in this.spies) {
            val player = this.minigame.server.player(spy) ?: continue
            event.addReceiver(player)
        }
        for ((uuid, mode) in this.modes) {
            val player = this.minigame.server.player(uuid) ?: continue
            if (MinigameChatMode.OwnTeam.canSendTo(event.player, player, mode, this.minigame)) {
                event.addReceiver(player)
            }
        }
    }

    private fun formatGlobalChatFor(player: ServerPlayer, message: PlayerFormattedChat): PlayerFormattedChat {
        if (this.minigame.players.isAdmin(player)) {
            return this.globalChatFormatter.format(player, this.adminChatFormatter.format(player, message))
        }
        if (this.minigame.players.isSpectating(player)) {
            return this.globalChatFormatter.format(player, this.spectatorChatFormatter.format(player, message))
        }
        return this.globalChatFormatter.format(player, message)
    }

    internal fun serialize(): JsonObject {
        val spies = JsonArray()
        for (spy in this.spies) {
            spies.add(spy.toString())
        }

        val modes = JsonArray()
        for ((uuid, mode) in this.modes) {
            val result = MinigameChatMode.OPTIONAL_CODEC.encodeStart(JsonOps.INSTANCE, Optional.ofNullable(mode))
            result.ifSuccess {
                val data = JsonObject()
                data.addProperty("uuid", uuid.toString())
                data.add("mode", it)
                modes.add(data)
            }
        }

        val json = JsonObject()
        json.add("spies", spies)
        json.add("selected_modes", modes)
        return json
    }

    internal fun deserialize(json: JsonObject) {
        this.spies.addAll(json.arrayOrDefault("spies").uuids())

        val modes = json.arrayOrDefault("selected_modes").objects()
        for (mode in modes) {
            val result = MinigameChatMode.CODEC.parse(JsonOps.INSTANCE, mode.obj("mode"))
            result.ifSuccess {
                this.modes[mode.uuid("uuid")] = it
            }
        }
    }

    internal fun onGlobalChatToggle() {
        this.minigame.commands.resendCommands()

        for (player in this.minigame.players) {
            val mode = this.modes[player.uuid]
            if (mode != null && mode != MinigameChatMode.Global) {
                if (this.minigame.settings.isChatGlobal) {
                    this.broadcastTo(MinigameChatMode.Global.switchedToMessage(player), player)
                } else {
                    this.broadcastTo(mode.switchedToMessage(player), player)
                }
            }
        }
    }

    private fun registerCommand() {
        this.minigame.commands.register(CommandTree.buildLiteral("chat") {
            requires { !minigame.settings.isChatGlobal && minigame.settings.enableChatCommand.get(it.playerOrException) }
            literal("team") {
                argument("team", TeamArgument.team()) {
                    requiresAdminOrPermission()
                    executes(::selectSpecificTeamChat)
                }
                executes { selectChat(it, MinigameChatMode.OwnTeam) }
            }
            literal("global") {
                executes { selectChat(it, MinigameChatMode.Global) }
            }
            literal("spectator") {
                requires { source ->
                    source.isMinigameAdminOrHasPermission() || source.isPlayerAnd(minigame.players::isSpectating)
                }
                executes { selectChat(it, MinigameChatMode.Spectator) }
            }
            literal("admin") {
                requiresAdminOrPermission()
                executes { selectChat(it, MinigameChatMode.Admin) }
            }
        })
    }

    private fun selectChat(context: CommandContext<CommandSourceStack>, mode: MinigameChatMode): Int {
        this.setChatModeFor(context.source.playerOrException, mode)
        return Command.SINGLE_SUCCESS
    }

    private fun selectSpecificTeamChat(context: CommandContext<CommandSourceStack>): Int {
        val team = TeamArgument.getTeam(context, "team")
        this.setChatModeFor(
            context.source.playerOrException,
            MinigameChatMode.Team.getOrCreate(team),
            true
        )
        return Command.SINGLE_SUCCESS
    }

    public companion object {
        public val MUTED: ResourceLocation = ResourceUtils.arcade("muted")
    }
}