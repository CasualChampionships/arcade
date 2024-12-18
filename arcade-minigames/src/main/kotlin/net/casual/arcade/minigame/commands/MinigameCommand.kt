/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.commands

import com.google.gson.JsonObject
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.serialization.JsonOps
import net.casual.arcade.commands.*
import net.casual.arcade.commands.arguments.EnumArgument
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.Minigames
import net.casual.arcade.minigame.chat.ChatFormatter
import net.casual.arcade.minigame.commands.arguments.*
import net.casual.arcade.minigame.commands.arguments.MinigameSettingsOptionArgument.Companion.INVALID_SETTING_OPTION
import net.casual.arcade.minigame.serialization.MinigameCreationContext
import net.casual.arcade.minigame.utils.AdvancementModifier
import net.casual.arcade.minigame.utils.MinigameUtils.countdown
import net.casual.arcade.minigame.utils.MinigameUtils.getMinigame
import net.casual.arcade.minigame.utils.RecipeModifier
import net.casual.arcade.scheduler.GlobalTickedScheduler
import net.casual.arcade.scheduler.task.Task
import net.casual.arcade.utils.ComponentUtils.green
import net.casual.arcade.utils.ComponentUtils.join
import net.casual.arcade.utils.ComponentUtils.suggestCommand
import net.casual.arcade.utils.JsonUtils
import net.casual.arcade.utils.impl.ConcatenatedList.Companion.concat
import net.casual.arcade.utils.time.MinecraftTimeUnit
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.commands.arguments.ComponentArgument
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.ResourceLocationArgument
import net.minecraft.commands.arguments.TeamArgument
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import java.util.*

internal object MinigameCommand: CommandTree {
    override fun create(buildContext: CommandBuildContext): LiteralArgumentBuilder<CommandSourceStack> {
        return CommandTree.buildLiteral("minigame") {
            requiresPermission(2)

            literal("list") {
                executes(::listMinigames)
            }
            literal("create") {
                argument("factory", MinigameFactoryCodecArgument.codec()) {
                    executes { createMinigame(it, JsonObject()) }
                    argument("data", MinigameFactoryDataArgument.data("factory")) {
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
                    literal("spectators") {
                        literal("set") {
                            argument("team", TeamArgument.team()) {
                                executes(::makeTeamSpectator)
                            }
                        }
                    }
                    literal("admins") {
                        literal("set") {
                            argument("team", TeamArgument.team()) {
                                executes(::makeTeamAdmin)
                            }
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
                    literal("announce") {
                        argument("announcement", ComponentArgument.textComponent(buildContext)) {
                            argument("title", ComponentArgument.textComponent(buildContext)) {
                                executes(::announce)
                            }
                            executes { announce(it, null) }
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
                        literal("set") {
                            literal("from") {
                                literal("option") {
                                    argument("option", MinigameSettingsOptionArgument.option("minigame", "setting")) {
                                        executes(::setMinigameSettingFromOption)
                                    }
                                }
                                literal("value") {
                                    argument("value", MinigameSettingValueArgument.value()) {
                                        executes(::setMinigameSettingFromValue)
                                    }
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
            literal("recipe") {
                argument("minigame", MinigameArgument.minigame()) {
                    argument("modifier", EnumArgument.enumeration<RecipeModifier>()) {
                        literal("only") {
                            argument("recipe", ResourceLocationArgument.id()) {
                                suggests { context, builder ->
                                    val minigame = MinigameArgument.getMinigame(context, "minigame")
                                    SharedSuggestionProvider.suggestResource(minigame.recipes.all().map { it.id.location() }, builder)
                                }
                                argument("player", EntityArgument.players()) {
                                    executes(::modifyMinigameRecipe)
                                }
                            }
                        }
                        literal("all") {
                            argument("player", EntityArgument.players()) {
                                executes(::modifyAllMinigameRecipes)
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
            return context.source.success(Component.translatable("minigame.command.list.none"))
        }
        val formatted = minigames.joinToString("\n") { "ID: ${it.id}, UUID: ${it.uuid}" }
        return context.source.success(formatted)
    }

    private fun selfJoinMinigame(context: CommandContext<CommandSourceStack>): Int {
        return this.addPlayersToMinigame(context, listOf(context.source.playerOrException))
    }

    private fun addPlayersToMinigame(
        context: CommandContext<CommandSourceStack>,
        players: Collection<ServerPlayer> = EntityArgument.getPlayers(context, "players")
    ): Int {
        return applyToPlayersInMinigame(
            context,
            players,
            { player, minigame -> minigame.players.add(player) },
            Component.translatable("minigame.command.players.add.fail"),
            { Component.translatable("minigame.command.players.add.success", it) }
        )
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
            return context.source.fail(
                Component.translatable("minigame.command.players.remove.fail")
            )
        }
        context.source.success(
            Component.translatable("minigame.command.players.remove.success", "$successes/$total")
        )
        return successes
    }

    private fun makeTeamAdmin(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        val team = TeamArgument.getTeam(context, "team")
        minigame.teams.setAdminTeam(team)
        return context.source.success(
            Component.translatable("minigame.command.team.admin", team.formattedDisplayName)
        )
    }

    private fun makeTeamSpectator(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        val team = TeamArgument.getTeam(context, "team")
        minigame.teams.setSpectatorTeam(team)
        return context.source.success(
            Component.translatable("minigame.command.team.spectator", team.formattedDisplayName)
        )
    }

    private fun setTeamEliminated(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        val team = TeamArgument.getTeam(context, "team")
        minigame.teams.addEliminatedTeam(team)
        return context.source.success(
            Component.translatable("minigame.command.team.eliminated.add", team.formattedDisplayName)
        )
    }

    private fun setTeamPlaying(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        val team = TeamArgument.getTeam(context, "team")
        minigame.teams.removeEliminatedTeam(team)
        return context.source.success(
            Component.translatable("minigame.command.team.eliminated.remove", team.formattedDisplayName)
        )
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
        return context.source.success(
            Component.translatable("minigame.command.chat.mute", "${i}/${players.size}")
        )
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
        return context.source.success(
            Component.translatable("minigame.command.chat.unmute", "${i}/${players.size}")
        )
    }

    private fun announce(
        context: CommandContext<CommandSourceStack>,
        title: Component? = ComponentArgument.getComponent(context, "title")
    ): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        val announcement = ComponentArgument.getComponent(context, "announcement")
        minigame.chat.broadcast(announcement, ChatFormatter.createAnnouncement(title))
        return context.source.success(Component.translatable("minigame.command.chat.announce.success"))
    }

    private fun selfAddSpy(context: CommandContext<CommandSourceStack>): Int {
        return this.addChatSpies(context, listOf(context.source.playerOrException))
    }

    private fun addChatSpies(
        context: CommandContext<CommandSourceStack>,
        players: Collection<ServerPlayer> = EntityArgument.getPlayers(context, "players")
    ): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        for (player in players) {
            minigame.chat.addSpy(player)
        }
        return context.source.success(
            Component.translatable("minigame.command.chat.spies.add")
        )
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
        return context.source.success(
            Component.translatable("minigame.command.chat.spies.remove")
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
            Component.translatable("minigame.command.spectators.add.fail"),
            { Component.translatable("minigame.command.spectators.add.success", it) }
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
            Component.translatable("minigame.command.spectators.remove.fail"),
            { Component.translatable("minigame.command.spectators.remove.success", it) }
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
            Component.translatable("minigame.command.admins.add.fail"),
            { Component.translatable("minigame.command.admins.add.success", it) }
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
            Component.translatable("minigame.command.admins.remove.fail"),
            { Component.translatable("minigame.command.admins.remove.success", it) }
        )
    }

    private fun applyToPlayersInMinigame(
        context: CommandContext<CommandSourceStack>,
        players: Collection<ServerPlayer>,
        function: (ServerPlayer, Minigame) -> Boolean,
        fail: Component,
        success: (String) -> Component
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

    private fun infoMinigame(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        return context.source.success(minigame.toString())
    }

    private fun infoPathMinigame(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        val path = MinigameInfoPathArgument.getPath(context, "path")
        val info = JsonUtils.GSON.toJson(minigame.property(path))
        return context.source.success(info)
    }

    private fun openMinigameSettings(context: CommandContext<CommandSourceStack>) {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        minigame.settings.gui(context.source.playerOrException).open()
    }

    private fun getMinigameSetting(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        val setting = MinigameSettingArgument.getSetting(context, "setting", minigame)
        return context.source.success(
            Component.translatable("minigame.command.setting.get", setting.name, setting.get().toString())
        )
    }

    private fun setMinigameSettingFromOption(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        val setting = MinigameSettingArgument.getSetting(context, "setting", minigame)
        val option = MinigameSettingsOptionArgument.getSettingsOption(context, "option")
        val value = setting.getOption(option) ?: throw INVALID_SETTING_OPTION.create()
        setting.setFromOption(option)
        return context.source.success(
            Component.translatable("minigame.command.setting.set.option", setting.name, option, value.toString())
        )
    }

    private fun setMinigameSettingFromValue(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        val setting = MinigameSettingArgument.getSetting(context, "setting", minigame)
        val value = MinigameSettingValueArgument.getSettingsValue(context, "value")
        setting.deserializeAndSet(value)
        return context.source.success(
            Component.translatable("minigame.command.setting.set.value", setting.get().toString())
        )
    }

    private fun modifyMinigameAdvancement(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        val modifier = EnumArgument.getEnumeration<AdvancementModifier>(context, "modifier")
        val id = ResourceLocationArgument.getId(context, "advancement")
        val player = EntityArgument.getPlayer(context, "player")
        val advancement = minigame.advancements.get(id)
            ?: return context.source.fail(Component.translatable("minigame.command.advancement.unknown"))
        return context.source.success(modifier.modifySingle(minigame, player, advancement))
    }

    private fun modifyAllMinigameAdvancements(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        val modifier = EnumArgument.getEnumeration<AdvancementModifier>(context, "modifier")
        val player = EntityArgument.getPlayer(context, "player")
        return context.source.success(modifier.modifyAll(minigame, player))
    }

    private fun modifyMinigameRecipe(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        val modifier = EnumArgument.getEnumeration<RecipeModifier>(context, "modifier")
        val id = ResourceLocationArgument.getId(context, "recipe")
        val player = EntityArgument.getPlayer(context, "player")
        val recipe = minigame.recipes.get(ResourceKey.create(Registries.RECIPE, id))
            ?: return context.source.fail(Component.translatable("minigame.command.recipe.unknown"))
        return context.source.success(modifier.modifySingle(minigame, player, recipe))
    }

    private fun modifyAllMinigameRecipes(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        val modifier = EnumArgument.getEnumeration<RecipeModifier>(context, "modifier")
        val player = EntityArgument.getPlayer(context, "player")
        return context.source.success(modifier.modifyAll(minigame, player))
    }

    private fun listPlayerTags(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        val player = EntityArgument.getPlayer(context, "player")
        val tags = minigame.tags.get(player).joinToString()
        return context.source.success(
            Component.translatable("minigame.command.tags.list", player.displayName, tags)
        )
    }

    private fun addPlayerTag(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        val player = EntityArgument.getPlayer(context, "player")
        val tag = ResourceLocationArgument.getId(context, "tag")

        if (!minigame.tags.add(player, tag)) {
            return context.source.fail(
                Component.translatable("minigame.command.tags.add.fail", player.displayName, tag.toString())
            )
        }
        return context.source.success(
            Component.translatable("minigame.command.tags.add.success", tag.toString(), player.displayName)
        )
    }

    private fun removePlayerTag(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        val player = EntityArgument.getPlayer(context, "player")
        val tag = ResourceLocationArgument.getId(context, "tag")

        if (!minigame.tags.remove(player, tag)) {
            return context.source.fail(
                Component.translatable("minigame.command.tags.remove.fail", player.displayName, tag.toString())
            )
        }
        return context.source.success(
            Component.translatable("minigame.command.tags.remove.success", tag.toString(), player.displayName)
        )
    }

    private fun getMinigamePhase(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        return context.source.success(
            Component.translatable("minigame.command.phase.get", minigame.id.toString(), minigame.phase.id)
        )
    }

    private fun setMinigamePhase(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        val phase = MinigamePhaseArgument.getPhase(context, "phase", minigame)
        minigame.setPhase(phase)
        return context.source.success(
            Component.translatable("minigame.command.phase.set", minigame.id.toString(), phase.id)
        )
    }

    private fun pauseMinigame(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        if (minigame.paused) {
            return context.source.fail(
                Component.translatable("minigame.command.pause.fail", minigame.id.toString())
            )
        }
        minigame.pause()
        return context.source.success(
            Component.translatable("minigame.command.pause.success", minigame.id.toString())
        )
    }

    private fun unpauseMinigame(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        if (!minigame.paused) {
            return context.source.fail(
                Component.translatable("minigame.command.unpause.fail", minigame.id.toString())
            )
        }
        minigame.unpause()
        return context.source.success(
            Component.translatable("minigame.command.unpause.success", minigame.id.toString())
        )
    }

    private fun readyUnpause(context: CommandContext<CommandSourceStack>, teams: Boolean): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        if (!minigame.paused) {
            return context.source.fail(
                Component.translatable("minigame.command.unpause.fail", minigame.id.toString())
            )
        }
        val callback = Task {
            val here = Component.translatable("minigame.command.unpause.here")
                .green().suggestCommand("/minigame unpause ${minigame.uuid} countdown 5 Seconds")

            val player = context.source.player
            val admins = if (player == null) minigame.players.admins else minigame.players.admins.concat(player)
            minigame.chat.broadcastTo(Component.translatable("minigame.command.unpause.countdown", here), admins)
        }
        if (teams) {
            minigame.ui.readier.areTeamsReady(minigame.teams.getPlayingTeams()).then(callback)
        } else {
            minigame.ui.readier.arePlayersReady(minigame.players.playing).then(callback)
        }
        context.source.success(Component.translatable("minigame.command.unpause.ready.success"))

        return context.source.success {
            val here = Component.translatable("minigame.command.unpause.here").green().function { context ->
                val awaiting = minigame.ui.readier.getUnreadyFormatted(context.server).join()
                val message = Component.translatable("minigame.command.unpause.ready.awaiting", awaiting)
                minigame.chat.broadcastTo(message, context.player)
            }
            if (teams) {
                Component.translatable("minigame.command.unpause.ready.teams", here)
            } else {
                Component.translatable("minigame.command.unpause.ready.players", here)
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
            return context.source.fail(
                Component.translatable("minigame.command.unpause.fail", minigame.id.toString())
            )
        }
        val duration = unit.duration(time)

        // We must use the global scheduler, because the minigame scheduler is paused
        val scheduler = GlobalTickedScheduler.temporaryScheduler(duration)
        minigame.ui.countdown.countdown(minigame, duration, scheduler = scheduler).then {
            minigame.unpause()
        }
        context.source.success(Component.translatable("minigame.command.unpause.countdown.success"))
        return context.source.success {
            val here = Component.translatable("minigame.command.unpause.here").green().singleUseFunction { context ->
                if (scheduler.cancelAll()) {
                    minigame.chat.broadcastTo(
                        Component.translatable("minigame.command.unpause.countdown.cancel.success"),
                        context.player
                    )
                }
            }
            Component.translatable("minigame.command.unpause.countdown.cancel", here)
        }
    }

    private fun createMinigame(
        context: CommandContext<CommandSourceStack>,
        data: JsonObject = MinigameFactoryDataArgument.getData(context, "data")
    ): Int {
        val factory = MinigameFactoryCodecArgument.getCodec(context, "factory")

        val result = factory.codec().parse(JsonOps.INSTANCE, data).result()
        if (result.isEmpty) {
            return context.source.fail(
                Component.translatable("minigame.command.create.fail")
            )
        }
        val minigame = result.get().create(MinigameCreationContext(context.source.server, UUID.randomUUID()))
        minigame.tryInitialize()
        return context.source.success(
            Component.translatable("minigame.command.create.success", minigame.id.toString(), minigame.uuid.toString())
        )
    }

    private fun closeMinigame(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        minigame.close()
        return context.source.success(
            Component.translatable("minigame.command.close.success", minigame.uuid.toString())
        )
    }

    private fun startMinigame(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        minigame.start()
        return context.source.success(
            Component.translatable("minigame.command.start.success", minigame.uuid.toString())
        )
    }
}