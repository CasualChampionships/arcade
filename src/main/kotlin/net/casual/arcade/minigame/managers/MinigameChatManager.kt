package net.casual.arcade.minigame.managers

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.mojang.brigadier.context.CommandContext
import com.mojang.serialization.JsonOps
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.casual.arcade.Arcade
import net.casual.arcade.chat.ChatFormatter
import net.casual.arcade.chat.PlayerChatFormatter
import net.casual.arcade.chat.PlayerFormattedChat
import net.casual.arcade.events.BuiltInEventPhases.DEFAULT
import net.casual.arcade.events.player.PlayerChatEvent
import net.casual.arcade.events.player.PlayerSystemMessageEvent
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.MinigameSettings
import net.casual.arcade.minigame.annotation.ListenerFlags
import net.casual.arcade.minigame.managers.chat.MinigameChatMode
import net.casual.arcade.utils.CommandUtils
import net.casual.arcade.utils.CommandUtils.argument
import net.casual.arcade.utils.CommandUtils.commandSuccess
import net.casual.arcade.utils.CommandUtils.success
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.ComponentUtils.red
import net.casual.arcade.utils.JsonUtils.arrayOrDefault
import net.casual.arcade.utils.JsonUtils.obj
import net.casual.arcade.utils.JsonUtils.objects
import net.casual.arcade.utils.JsonUtils.uuid
import net.casual.arcade.utils.JsonUtils.uuids
import net.casual.arcade.utils.MinigameUtils.isMinigameAdminOrHasPermission
import net.casual.arcade.utils.MinigameUtils.isPlayerAnd
import net.casual.arcade.utils.MinigameUtils.requiresAdminOrPermission
import net.casual.arcade.utils.PlayerUtils.getChatPrefix
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

    internal val modes = Object2ObjectOpenHashMap<UUID, MinigameChatMode>()
    internal val spies = ObjectOpenHashSet<UUID>()

    init {
        this.minigame.events.register<PlayerChatEvent>(1_000, DEFAULT, ListenerFlags.NONE, this::onGlobalPlayerChat)
        this.minigame.events.register<PlayerSystemMessageEvent>(1_000, DEFAULT, ListenerFlags.NONE, this::onGlobalSystemChat)
        this.minigame.events.register<PlayerChatEvent> { this.onPlayerChat(it) }

        this.modes.defaultReturnValue(MinigameChatMode.OwnTeam)

        this.registerCommand()
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

    private fun onGlobalSystemChat(event: PlayerSystemMessageEvent) {
        if (this.minigame.settings.isChatMuted.get(event.player)) {
            event.cancel()
            return
        }

        val causer = event.causer
        if (causer != null) {
            if (!this.minigame.settings.canCrossChat && !this.minigame.players.has(causer)) {
                event.cancel()
            }
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

        val mode = this.modes[player.uuid]

        val content = event.rawMessage
        val exclaimed = content.startsWith("!")
        if (exclaimed || mode == null) {
            val trimmed = if (exclaimed) content.substring(1) else content
            if (trimmed.isNotBlank()) {
                val (decorated, prefix) = this.formatGlobalChatFor(player, trimmed.trim().literal())
                event.replaceMessage(decorated, prefix)
            } else {
                event.cancel()
            }
            return
        }

        val formatter = mode.getChatFormatter(this)
        val (decorated, prefix) = formatter.format(player, message.decoratedContent())
        event.replaceMessage(decorated, prefix)
        event.addFilter { this.isSpy(it) || mode.canSendTo(it, player, this.minigame) }
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

    internal fun serialize(): JsonObject {
        val spies = JsonArray()
        for (spy in this.spies) {
            spies.add(spy.toString())
        }

        val modes = JsonArray()
        for ((uuid, mode) in this.modes) {
            val result = MinigameChatMode.CODEC.encodeStart(JsonOps.INSTANCE, mode)
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

    private fun registerCommand() {
        this.minigame.commands.register(CommandUtils.buildLiteral("chat") {
            literal("team") {
                argument("team", TeamArgument.team()) {
                    requiresAdminOrPermission()
                    executes(::selectSpecificTeamChat)
                }
                executes(::selectOwnTeamChat)
            }
            literal("global") {
                executes(::selectGlobalChat)
            }
            literal("spectator") {
                requires { source ->
                    source.isMinigameAdminOrHasPermission() || source.isPlayerAnd(minigame.players::isSpectating)
                }
                executes(::selectSpectatorChat)
            }
            literal("admin") {
                requiresAdminOrPermission()
                executes(::selectAdminChat)
            }
        })
    }

    private fun selectSpecificTeamChat(context: CommandContext<CommandSourceStack>): Int {
        val team = TeamArgument.getTeam(context, "team")
        return this.selectChat(
            context,
            MinigameChatMode.Team.getOrCreate(team),
            Component.translatable("minigame.chat.mode.switch.specificTeam", team.formattedDisplayName)
        )
    }

    private fun selectOwnTeamChat(context: CommandContext<CommandSourceStack>): Int {
        return this.selectChat(
            context,
            MinigameChatMode.OwnTeam,
            Component.translatable("minigame.chat.mode.switch.ownTeam")
        )
    }

    private fun selectSpectatorChat(context: CommandContext<CommandSourceStack>): Int {
        return this.selectChat(
            context,
            MinigameChatMode.Spectator,
            Component.translatable("minigame.chat.mode.switch.spectator")
        )
    }

    private fun selectAdminChat(context: CommandContext<CommandSourceStack>): Int {
        return this.selectChat(
            context,
            MinigameChatMode.Admin,
            Component.translatable("minigame.chat.mode.switch.admin")
        )
    }

    private fun selectGlobalChat(context: CommandContext<CommandSourceStack>): Int {
        return this.selectChat(
            context,
            null,
            Component.translatable("minigame.chat.mode.switch.global")
        )
    }

    private fun selectChat(
        context: CommandContext<CommandSourceStack>,
        mode: MinigameChatMode?,
        component: Component
    ): Int {
        val player = context.source.playerOrException
        this.modes[player.uuid] = mode
        return this.broadcastTo(component, player).commandSuccess()
    }

    public companion object {
        public val MUTED: ResourceLocation = Arcade.id("muted")
    }
}