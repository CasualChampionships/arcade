package net.casual.arcade.minigame.commands

import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import net.casual.arcade.commands.*
import net.casual.arcade.commands.arguments.EnumArgument
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.Minigames
import net.casual.arcade.minigame.commands.arguments.*
import net.casual.arcade.minigame.commands.arguments.MinigameSettingsOptionArgument.Companion.INVALID_SETTING_OPTION
import net.casual.arcade.minigame.serialization.MinigameCreationContext
import net.casual.arcade.minigame.utils.AdvancementModifier
import net.casual.arcade.minigame.utils.MinigameUtils.countdown
import net.casual.arcade.minigame.utils.MinigameUtils.getMinigame
import net.casual.arcade.scheduler.GlobalTickedScheduler
import net.casual.arcade.scheduler.task.Task
import net.casual.arcade.utils.ComponentUtils.green
import net.casual.arcade.utils.ComponentUtils.join
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.ComponentUtils.suggestCommand
import net.casual.arcade.utils.JsonUtils
import net.casual.arcade.utils.impl.ConcatenatedList.Companion.concat
import net.casual.arcade.utils.time.MinecraftTimeUnit
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.ResourceLocationArgument
import net.minecraft.commands.arguments.TeamArgument
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer

internal object MinigameCommand: CommandTree {
    override fun register(dispatcher: CommandDispatcher<CommandSourceStack>, buildContext: CommandBuildContext) {
        dispatcher.registerLiteral("minigame") {
            requiresPermission(4)

            literal("list") {
                executes(::listMinigames)
            }
            literal("create") {
                argument("factory", MinigameFactoryArgument.factory()) {
                    executes { createMinigame(it, null) }
                    argument("parameters", StringArgumentType.greedyString()) {
                        executes(::createMinigame)
                    }
                }
            }
            literal("join") {
                argument("minigame", MinigameArgument.minigame()) {
                    executes(::selfJoinMinigame)
                    argument("players", EntityArgument.players()) {
                        executes(::addPlayersToMinigame)
                    }
                }
            }
            literal("leave") {
                executes(::selfLeaveMinigame)
                argument("players", EntityArgument.players()) {
                    executes(::removePlayersFromMinigame)
                }
            }
            literal("start") {
                argument("minigame", MinigameArgument.minigame()) {
                    executes(::startMinigame)
                }
            }
            literal("close") {
                argument("minigame", MinigameArgument.minigame()) {
                    executes(::closeMinigame)
                }
            }
            literal("info") {
                argument("minigame", MinigameArgument.minigame()) {
                    executes(::infoMinigame)
                    argument("path", MinigameInfoPathArgument.path("minigame")) {
                        executes(::infoPathMinigame)
                    }
                }
            }
            literal("team") {
                argument("minigame", MinigameArgument.minigame()) {
                    literal("spectators").literal("set") {
                        argument("team", TeamArgument.team()) {
                            executes(::makeTeamSpectator)
                        }
                    }
                    literal("admins").literal("set") {
                        argument("team", TeamArgument.team()) {
                            executes(::makeTeamAdmin)
                        }
                    }
                    literal("eliminated") {
                        literal("add") {
                            argument("team", TeamArgument.team()) {
                                executes(::setTeamEliminated)
                            }
                        }
                        literal("remove") {
                            argument("team", TeamArgument.team()) {
                                executes(::setTeamPlaying)
                            }
                        }
                    }
                }
            }
            literal("chat") {
                argument("minigame", MinigameArgument.minigame()) {
                    literal("spies") {
                        literal("add") {
                            executes(::selfAddSpy)
                            argument("players", EntityArgument.players()) {
                                executes(::addChatSpies)
                            }
                        }
                        literal("remove") {
                            executes(::selfRemoveSpy)
                            argument("players", EntityArgument.players()) {
                                executes(::removeChatSpies)
                            }
                        }
                    }
                    literal("mute") {
                        argument("players", EntityArgument.players()) {
                            executes(::mute)
                        }
                    }
                    literal("unmute") {
                        argument("players", EntityArgument.players()) {
                            executes(::unmute)
                        }
                    }
                }
            }
            literal("spectating") {
                argument("minigame", MinigameArgument.minigame()) {
                    literal("add") {
                        executes(::selfSpectateMinigame)
                        argument("players", EntityArgument.players()) {
                            executes(::addPlayersToSpectate)
                        }
                    }
                    literal("remove") {
                        executes(::selfPlayingMinigame)
                        argument("players", EntityArgument.players()) {
                            executes(::addPlayersToPlaying)
                        }
                    }
                }
            }
            literal("admin") {
                argument("minigame", MinigameArgument.minigame()) {
                    literal("add") {
                        executes(::selfAdminMinigame)
                        argument("players", EntityArgument.players()) {
                            executes(::addPlayersToAdmin)
                        }
                    }
                    literal("remove") {
                        executes(::selfUnAdminMinigame)
                        argument("players", EntityArgument.players()) {
                            executes(::removePlayersFromAdmin)
                        }
                    }
                }
            }
            literal("settings") {
                argument("minigame", MinigameArgument.minigame()) {
                    executes(::openMinigameSettings)
                    argument("setting", MinigameSettingArgument.setting("minigame")) {
                        executes(::getMinigameSetting)
                        literal("set").literal("from") {
                            literal("option") {
                                argument("option", MinigameSettingsOptionArgument.option("minigame", "setting")) {
                                    executes(::setMinigameSettingFromOption)
                                }
                            }
                            literal("value") {
                                argument("value", MinigameSettingValueArgument.value(buildContext)) {
                                    executes(::setMinigameSettingFromValue)
                                }
                            }
                        }
                    }
                }
            }
            literal("advancement") {
                argument("minigame", MinigameArgument.minigame()) {
                    argument("modifier", EnumArgument.enumeration<AdvancementModifier>()) {
                        literal("only") {
                            argument("advancement", ResourceLocationArgument.id()) {
                                suggests { context, builder ->
                                    val minigame = MinigameArgument.getMinigame(context, "minigame")
                                    SharedSuggestionProvider.suggestResource(minigame.advancements.all().map { it.id }, builder)
                                }
                                argument("player", EntityArgument.players()) {
                                    executes(::modifyMinigameAdvancement)
                                }
                            }
                        }
                        literal("all") {
                            argument("player", EntityArgument.players()) {
                                executes(::modifyAllMinigameAdvancements)
                            }
                        }
                    }
                }
            }
            literal("tags") {
                argument("minigame", MinigameArgument.minigame()) {
                    argument("player", EntityArgument.player()) {
                        literal("add") {
                            argument("tag", ResourceLocationArgument.id()) {
                                executes(::addPlayerTag)
                            }
                        }
                        literal("remove") {
                            argument("tag", ResourceLocationArgument.id()) {
                                suggests { context ->
                                    val minigame = MinigameArgument.getMinigame(context, "minigame")
                                    val player = EntityArgument.getPlayer(context, "player")
                                    minigame.tags.get(player).map(ResourceLocation::toString)
                                }
                                executes(::removePlayerTag)
                            }
                        }
                        literal("list") {
                            executes(::listPlayerTags)
                        }
                    }
                }
            }
            literal("phase") {
                argument("minigame", MinigameArgument.minigame()) {
                    executes(::getMinigamePhase)
                    literal("set") {
                        argument("phase", MinigamePhaseArgument.name("minigame")) {
                            executes(::setMinigamePhase)
                        }
                    }
                }
            }
            literal("pause") {
                argument("minigame", MinigameArgument.minigame()) {
                    executes(::pauseMinigame)
                }
            }
            literal("unpause") {
                argument("minigame", MinigameArgument.minigame()) {
                    executes(::unpauseMinigame)
                    literal("countdown") {
                        executes { unpauseWithCountdown(it, 10, MinecraftTimeUnit.Seconds) }
                        argument("time", IntegerArgumentType.integer(1)) {
                            argument("unit", EnumArgument.enumeration<MinecraftTimeUnit>()) {
                                executes(::unpauseWithCountdown)
                            }
                        }
                    }
                    literal("ready") {
                        literal("players") {
                            executes { readyUnpause(it, false) }
                        }
                        literal("teams") {
                            executes { readyUnpause(it, true) }
                        }
                    }
                }
            }
        }
    }

    private fun listMinigames(context: CommandContext<CommandSourceStack>): Int {
        val minigames = Minigames.all()
        if (minigames.isEmpty()) {
            return context.source.success("There are no running minigames!")
        }
        val formatted = minigames.joinToString("\n") { "ID: ${it.id}, UUID: ${it.uuid}" }
        return context.source.success(formatted)
    }

    private fun selfJoinMinigame(context: CommandContext<CommandSourceStack>): Int {
        return this.addPlayersToMinigame(context, listOf(context.source.playerOrException))
    }

    private fun makeTeamAdmin(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        val team = TeamArgument.getTeam(context, "team")
        minigame.teams.setAdminTeam(team)
        return context.source.success("Successfully set ${team.name} to be the admin team")
    }

    private fun makeTeamSpectator(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        val team = TeamArgument.getTeam(context, "team")
        minigame.teams.setSpectatorTeam(team)
        return context.source.success("Successfully set ${team.name} to be the spectator team")
    }

    private fun setTeamEliminated(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        val team = TeamArgument.getTeam(context, "team")
        minigame.teams.addEliminatedTeam(team)
        return context.source.success("Successfully set ${team.name} to be eliminated")
    }

    private fun setTeamPlaying(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        val team = TeamArgument.getTeam(context, "team")
        minigame.teams.removeEliminatedTeam(team)
        return context.source.success("Successfully set ${team.name} to be playing")
    }

    private fun selfAddSpy(context: CommandContext<CommandSourceStack>): Int {
        return this.addChatSpies(context, listOf(context.source.playerOrException))
    }

    private fun mute(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        val players = EntityArgument.getPlayers(context, "players")
        var i = 0
        for (player in players) {
            if (minigame.chat.mute(player)) {
                i++
            }
        }
        return context.source.success("Successfully muted ${i}/${players.size} players")
    }

    private fun unmute(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        val players = EntityArgument.getPlayers(context, "players")
        var i = 0
        for (player in players) {
            if (minigame.chat.unmute(player)) {
                i++
            }
        }
        return context.source.success("Successfully unmuted ${i}/${players.size} players")
    }

    private fun addChatSpies(
        context: CommandContext<CommandSourceStack>,
        players: Collection<ServerPlayer> = EntityArgument.getPlayers(context, "players")
    ): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        for (player in players) {
            minigame.chat.addSpy(player)
        }
        return context.source.success("Successfully added players as spies!")
    }

    private fun selfRemoveSpy(context: CommandContext<CommandSourceStack>): Int {
        return this.removeChatSpies(context, listOf(context.source.playerOrException))
    }

    private fun removeChatSpies(
        context: CommandContext<CommandSourceStack>,
        players: Collection<ServerPlayer> = EntityArgument.getPlayers(context, "players")
    ): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        for (player in players) {
            minigame.chat.removeSpy(player)
        }
        return context.source.success("Successfully removed players as spies!")
    }

    private fun addPlayersToMinigame(
        context: CommandContext<CommandSourceStack>,
        players: Collection<ServerPlayer> = EntityArgument.getPlayers(context, "players")
    ): Int {
        return applyToPlayersInMinigame(
            context,
            players,
            { player, minigame -> minigame.players.add(player) },
            "Failed to add any players to minigame",
            { "Successfully added $it players to minigame" }
        )
    }

    private fun selfSpectateMinigame(context: CommandContext<CommandSourceStack>): Int {
        return this.addPlayersToSpectate(context, listOf(context.source.playerOrException))
    }

    private fun addPlayersToSpectate(
        context: CommandContext<CommandSourceStack>,
        players: Collection<ServerPlayer> = EntityArgument.getPlayers(context, "players")
    ): Int {
        return applyToPlayersInMinigame(
            context,
            players,
            { player, minigame -> minigame.players.setSpectating(player) },
            "Failed to make players spectate",
            { "Successfully made $it players spectate" }
        )
    }

    private fun selfPlayingMinigame(context: CommandContext<CommandSourceStack>): Int {
        return this.addPlayersToPlaying(context, listOf(context.source.playerOrException))
    }

    private fun addPlayersToPlaying(
        context: CommandContext<CommandSourceStack>,
        players: Collection<ServerPlayer> = EntityArgument.getPlayers(context, "players")
    ): Int {
        return applyToPlayersInMinigame(
            context,
            players,
            { player, minigame -> minigame.players.setPlaying(player) },
            "Failed to make players playing",
            { "Successfully made $it players playing" }
        )
    }

    private fun selfAdminMinigame(context: CommandContext<CommandSourceStack>): Int {
        return this.addPlayersToAdmin(context, listOf(context.source.playerOrException))
    }

    private fun addPlayersToAdmin(
        context: CommandContext<CommandSourceStack>,
        players: Collection<ServerPlayer> = EntityArgument.getPlayers(context, "players")
    ): Int {
        return applyToPlayersInMinigame(
            context,
            players,
            { player, minigame -> minigame.players.addAdmin(player) },
            "Failed to make players admin",
            { "Successfully made $it players admin" }
        )
    }

    private fun selfUnAdminMinigame(context: CommandContext<CommandSourceStack>): Int {
        return this.removePlayersFromAdmin(context, listOf(context.source.playerOrException))
    }

    private fun removePlayersFromAdmin(
        context: CommandContext<CommandSourceStack>,
        players: Collection<ServerPlayer> = EntityArgument.getPlayers(context, "players")
    ): Int {
        return applyToPlayersInMinigame(
            context,
            players,
            { player, minigame -> minigame.players.removeAdmin(player) },
            "Failed to remove players admin",
            { "Successfully removed $it players admin" }
        )
    }

    private fun applyToPlayersInMinigame(
        context: CommandContext<CommandSourceStack>,
        players: Collection<ServerPlayer>,
        function: (ServerPlayer, Minigame<*>) -> Boolean,
        fail: String,
        success: (String) -> String
    ): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        val total = players.size
        var successes = 0
        for (player in players) {
            if (function(player, minigame)) {
                successes++
            }
        }
        if (successes == 0) {
            return context.source.fail(fail)
        }
        context.source.success(success("$successes/$total"))
        return successes
    }

    private fun selfLeaveMinigame(context: CommandContext<CommandSourceStack>): Int {
        return this.removePlayersFromMinigame(listOf(context.source.playerOrException), context)
    }

    private fun removePlayersFromMinigame(context: CommandContext<CommandSourceStack>): Int {
        val players = EntityArgument.getPlayers(context, "players")
        return this.removePlayersFromMinigame(players, context)
    }

    private fun removePlayersFromMinigame(players: Collection<ServerPlayer>, context: CommandContext<CommandSourceStack>): Int {
        val total = players.size
        var successes = 0
        for (player in players) {
            val minigame = player.getMinigame()
            if (minigame !== null) {
                minigame.players.remove(player)
                successes++
            }
        }
        if (successes == 0) {
            return context.source.fail("Failed to remove any players from minigames")
        }
        context.source.success("Successfully removed $successes/$total players from minigames")
        return successes
    }

    private fun infoMinigame(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        return context.source.success(minigame.toString())
    }

    private fun infoPathMinigame(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        val path = MinigameInfoPathArgument.getPath(context, "path")
        val info = JsonUtils.GSON.toJson(minigame.getDebugInfo().get(path))
        return context.source.success(info)
    }

    private fun openMinigameSettings(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        return minigame.settings.gui(context.source.playerOrException).open().commandSuccess()
    }

    private fun getMinigameSetting(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        val setting = MinigameSettingArgument.getSetting(context, "setting", minigame)
        return context.source.success("Setting ${setting.name} for minigame ${minigame.id} is set to ${setting.get()}")
    }

    private fun setMinigameSettingFromOption(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        val setting= MinigameSettingArgument.getSetting(context, "setting", minigame)
        val option = MinigameSettingsOptionArgument.getSettingsOption(context, "option")
        val value = setting.getOption(option) ?: throw INVALID_SETTING_OPTION.create()
        setting.setFromOption(option)
        return context.source.success("Setting ${setting.name} for minigame ${minigame.id} set to option $option ($value)")
    }

    private fun setMinigameSettingFromValue(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        val setting = MinigameSettingArgument.getSetting(context, "setting", minigame)
        val value = MinigameSettingValueArgument.getSettingsValue(context, "value")
        setting.deserializeAndSet(value)
        return context.source.success("Setting ${setting.name} for minigame ${minigame.id} set to ${setting.get()}")
    }

    private fun modifyMinigameAdvancement(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        val modifier = EnumArgument.getEnumeration<AdvancementModifier>(context, "modifier")
        val id = ResourceLocationArgument.getId(context, "advancement")
        val player = EntityArgument.getPlayer(context, "player")
        val advancement = minigame.advancements.get(id)
            ?: return context.source.fail("No such advancement $id exists")
        modifier.modify(player, advancement)
        return context.source.success(modifier.singleSuccessMessage(player, advancement))
    }

    private fun modifyAllMinigameAdvancements(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        val modifier = EnumArgument.getEnumeration<AdvancementModifier>(context, "modifier")
        val player = EntityArgument.getPlayer(context, "player")
        for (advancement in minigame.advancements.all()) {
            modifier.modify(player, advancement)
        }
        return context.source.success(modifier.allSuccessMessage(player))
    }

    private fun listPlayerTags(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        val player = EntityArgument.getPlayer(context, "player")
        val tags = minigame.tags.get(player).joinToString()
        return context.source.success("Tags for ${player.scoreboardName}: $tags")
    }

    private fun addPlayerTag(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        val player = EntityArgument.getPlayer(context, "player")
        val tag = ResourceLocationArgument.getId(context, "tag")

        if (!minigame.tags.add(player, tag)) {
            return context.source.fail("${player.scoreboardName} already had tag $tag")
        }
        return context.source.success("Successfully added tag $tag to ${player.scoreboardName}")
    }

    private fun removePlayerTag(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        val player = EntityArgument.getPlayer(context, "player")
        val tag = ResourceLocationArgument.getId(context, "tag")

        if (!minigame.tags.remove(player, tag)) {
            return context.source.fail("${player.scoreboardName} did not have tag $tag")
        }
        return context.source.success("Successfully removed tag $tag for ${player.scoreboardName}")
    }

    private fun getMinigamePhase(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        return context.source.success("The phase of minigame ${minigame.id} is ${minigame.phase.id}")
    }

    private fun <M: Minigame<M>> setMinigamePhase(context: CommandContext<CommandSourceStack>): Int {
        @Suppress("UNCHECKED_CAST")
        val minigame = MinigameArgument.getMinigame(context, "minigame") as Minigame<M>
        val phase = MinigamePhaseArgument.getPhase(context, "phase", minigame)
        minigame.setPhase(phase)
        return context.source.success("Successfully set phase of minigame ${minigame.id} to ${phase.id}")
    }

    private fun pauseMinigame(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        if (minigame.paused) {
            return context.source.fail("Minigame ${minigame.id} was already paused")
        }
        minigame.pause()
        return context.source.success("Successfully paused minigame ${minigame.id}")
    }

    private fun unpauseMinigame(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        if (!minigame.paused) {
            return context.source.fail("Minigame ${minigame.id} was already unpaused")
        }
        minigame.unpause()
        return context.source.success("Successfully unpaused minigame ${minigame.id}")
    }

    private fun readyUnpause(context: CommandContext<CommandSourceStack>, teams: Boolean): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        if (!minigame.paused) {
            return context.source.fail("Minigame ${minigame.id} was already unpaused")
        }
        val callback = Task {
            val message = "Click ".literal().apply {
                append("[here]".literal().green().suggestCommand("/minigame unpause ${minigame.uuid} countdown 5 Seconds"))
                append(" to start the unpause countdown!")
            }
            val player = context.source.player
            val admins = if (player == null) minigame.players.admins else minigame.players.admins.concat(player)
            minigame.chat.broadcastTo(message, admins)
        }
        if (teams) {
            minigame.ui.readier.areTeamsReady(minigame.teams.getPlayingTeams()).then(callback)
        } else {
            minigame.ui.readier.arePlayersReady(minigame.players.playing).then(callback)
        }
        return context.source.success {
            "Successfully broadcasted unpause ready check, click ".literal().apply {
                append("[here]".literal().green().function { context ->
                    val awaiting = minigame.ui.readier.getUnreadyFormatted(context.server).join()
                    val message = "Awaiting the following ${if (teams) "teams" else "players"}: ".literal().append(awaiting)
                    minigame.chat.broadcastTo(message, context.player)
                })
                append(" to view the awaiting ${if (teams) "teams" else "players"}")
            }
        }
    }

    private fun unpauseWithCountdown(
        context: CommandContext<CommandSourceStack>,
        time: Int = IntegerArgumentType.getInteger(context, "time"),
        unit: MinecraftTimeUnit = EnumArgument.getEnumeration(context, "unit", MinecraftTimeUnit::class.java)
    ): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        if (!minigame.paused) {
            return context.source.fail("Minigame ${minigame.id} was already unpaused")
        }
        val duration = unit.duration(time)

        // We must use the global scheduler, because the minigame scheduler is paused
        val scheduler = GlobalTickedScheduler.temporaryScheduler(duration)
        minigame.ui.countdown.countdown(minigame, duration, scheduler = scheduler).then {
            minigame.unpause()
        }
        return context.source.success {
            "Successfully started countdown, click ".literal().apply {
                append("[here]".literal().green().singleUseFunction { context ->
                    if (scheduler.cancelAll()) {
                        minigame.chat.broadcastTo("Successfully cancelled the countdown".literal(), context.player)
                    }
                })
                append(" to cancel the countdown")
            }
        }
    }

    private fun createMinigame(
        context: CommandContext<CommandSourceStack>,
        raw: String? = StringArgumentType.getString(context, "parameters")
    ): Int {
        val factory = MinigameFactoryArgument.getFactory(context, "factory")
        val parameters = if (raw != null) {
            try {
                JsonUtils.GSON.fromJson(raw, JsonObject::class.java)
            } catch (_: JsonParseException) {
                return context.source.fail("Failed to parse parameters, invalid JSON object")
            }
        } else {
            JsonObject()
        }

        val minigame = factory.create(MinigameCreationContext(context.source.server, parameters))
        minigame.tryInitialize()
        return context.source.success("Successfully created minigame ${minigame.id} with uuid ${minigame.uuid}")
    }

    private fun closeMinigame(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        minigame.close()
        return context.source.success("Successfully closed minigame ${minigame.id}")
    }

    private fun startMinigame(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        minigame.start()
        return context.source.success("Successfully started minigame ${minigame.id}")
    }
}