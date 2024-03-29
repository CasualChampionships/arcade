package net.casual.arcade.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.casual.arcade.commands.arguments.EnumArgument
import net.casual.arcade.commands.arguments.MinigameArgument
import net.casual.arcade.commands.arguments.MinigameArgument.PhaseName.Companion.INVALID_PHASE_NAME
import net.casual.arcade.commands.arguments.MinigameArgument.SettingsName.Companion.INVALID_SETTING_NAME
import net.casual.arcade.commands.arguments.MinigameArgument.SettingsOption.Companion.INVALID_SETTING_OPTION
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.Minigames
import net.casual.arcade.minigame.serialization.MinigameCreationContext
import net.casual.arcade.scheduler.GlobalTickedScheduler
import net.casual.arcade.scheduler.MinecraftTimeUnit
import net.casual.arcade.utils.CommandUtils.commandSuccess
import net.casual.arcade.utils.CommandUtils.fail
import net.casual.arcade.utils.CommandUtils.success
import net.casual.arcade.utils.ComponentUtils.function
import net.casual.arcade.utils.ComponentUtils.green
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.ComponentUtils.suggestCommand
import net.casual.arcade.utils.JsonUtils
import net.casual.arcade.utils.MinigameUtils.countdown
import net.casual.arcade.utils.MinigameUtils.getMinigame
import net.casual.arcade.utils.PlayerUtils.toComponent
import net.casual.arcade.utils.TeamUtils.toComponent
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.ResourceLocationArgument
import net.minecraft.commands.arguments.TeamArgument
import net.minecraft.server.level.ServerPlayer
import java.util.concurrent.CompletableFuture

internal object MinigameCommand: Command {
    override fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("minigame").requires {
                it.hasPermission(4)
            }.then(
                Commands.literal("list").executes(this::listMinigames)
            ).then(
                Commands.literal("join").then(
                    Commands.argument("minigame", MinigameArgument.minigame()).then(
                        Commands.argument("players", EntityArgument.players()).executes(this::addPlayersToMinigame)
                    ).executes(this::selfJoinMinigame)
                )
            ).then(
                Commands.literal("team").then(
                    Commands.argument("minigame", MinigameArgument.minigame()).then(
                        Commands.argument("team", TeamArgument.team()).then(
                            Commands.literal("admin").executes(this::makeTeamAdmin)
                        ).then(
                            Commands.literal("spectator").executes(this::makeTeamSpectator)
                        ).then(
                            Commands.literal("eliminated").executes(this::setTeamEliminated)
                        ).then(
                            Commands.literal("playing").executes(this::setTeamPlaying)
                        )
                    )
                )
            ).then(
                Commands.literal("spectate").then(
                    Commands.argument("minigame", MinigameArgument.minigame()).then(
                        Commands.argument("players", EntityArgument.players()).executes(this::addPlayersToSpectate)
                    ).executes(this::selfSpectateMinigame)
                )
            ).then(
                Commands.literal("playing").then(
                    Commands.argument("minigame", MinigameArgument.minigame()).then(
                        Commands.argument("players", EntityArgument.players()).executes(this::addPlayersToPlaying)
                    ).executes(this::selfPlayingMinigame)
                )
            ).then(
                Commands.literal("admin").then(
                    Commands.argument("minigame", MinigameArgument.minigame()).then(
                        Commands.argument("players", EntityArgument.players()).executes(this::addPlayersToAdmin)
                    ).executes(this::selfAdminMinigame)
                )
            ).then(
                Commands.literal("unadmin").then(
                    Commands.argument("minigame", MinigameArgument.minigame()).then(
                        Commands.argument("players", EntityArgument.players()).executes(this::removePlayersFromAdmin)
                    ).executes(this::selfUnAdminMinigame)
                )
            ).then(
                Commands.literal("leave").then(
                    Commands.argument("players", EntityArgument.players()).executes(this::otherLeaveMinigame)
                ).executes(this::selfLeaveMinigame)
            ).then(
                Commands.literal("info").then(
                    Commands.argument("minigame", MinigameArgument.minigame()).then(
                        Commands.argument("path", MinigameArgument.InfoPath.path("minigame")).executes(this::infoPathMinigame)
                    ).executes(this::infoMinigame)
                )
            ).then(
                Commands.literal("settings").then(
                    Commands.argument("minigame", MinigameArgument.minigame()).then(
                        Commands.literal("get").then(
                            Commands.argument("setting", MinigameArgument.SettingsName.name("minigame")).executes(this::getMinigameSetting)
                        )
                    ).then(
                        Commands.literal("set").then(
                            Commands.argument("setting", MinigameArgument.SettingsName.name("minigame")).then(
                                Commands.literal("from").then(
                                    Commands.literal("option").then(
                                        Commands.argument("option", MinigameArgument.SettingsOption.option("minigame", "setting")).executes(this::setMinigameSettingFromOption)
                                    )
                                ).then(
                                    Commands.literal("value").then(
                                        Commands.argument("value", MinigameArgument.SettingsValue.value()).executes(this::setMinigameSettingFromValue)
                                    )
                                )
                            )
                        )
                    ).executes(this::openMinigameSettings)
                )
            ).then(
                Commands.literal("tags").then(
                    Commands.argument("minigame", MinigameArgument.minigame()).then(
                        Commands.argument("player", EntityArgument.player()).then(
                            Commands.literal("add").then(
                                Commands.argument("tag", ResourceLocationArgument.id()).executes(this::addPlayerTag)
                            )
                        ).then(
                            Commands.literal("remove").then(
                                Commands.argument("tag", ResourceLocationArgument.id()).suggests(this::suggestExistingTags).executes(this::removePlayerTag)
                            )
                        ).executes(this::listPlayerTags)
                    )
                )
            ).then(
                Commands.literal("phase").then(
                    Commands.argument("minigame", MinigameArgument.minigame()).then(
                        Commands.literal("get").executes(this::getMinigamePhase)
                    ).then(
                        Commands.literal("set").then(
                            Commands.argument("phase", MinigameArgument.PhaseName.name("minigame")).executes(this::setMinigamePhase)
                        )
                    )
                )
            ).then(
                Commands.literal("pause").then(
                    Commands.argument("minigame", MinigameArgument.minigame()).executes(this::pauseMinigame)
                )
            ).then(
                Commands.literal("unpause").then(
                    Commands.argument("minigame", MinigameArgument.minigame()).then(
                        Commands.literal("countdown").then(
                            Commands.argument("time", IntegerArgumentType.integer(1)).then(
                                Commands.argument("unit", EnumArgument.enumeration(MinecraftTimeUnit::class.java)).executes(this::unpauseWithCountdown)
                            )
                        ).executes { this.unpauseWithCountdown(it, 10, MinecraftTimeUnit.Seconds) }
                    ).then(
                        Commands.literal("ready").then(
                            Commands.literal("players").executes { this.readyUnpause(it, false) }
                        ).then(
                            Commands.literal("teams").executes { this.readyUnpause(it, true) }
                        )
                    ).executes(this::unpauseMinigame)
                )
            ).then(
                Commands.literal("create").then(
                    Commands.argument("factory", MinigameArgument.Factory.factory()).executes(this::createMinigame)
                )
            ).then(
                Commands.literal("close").then(
                    Commands.argument("minigame", MinigameArgument.minigame()).executes(this::closeMinigame)
                )
            ).then(
                Commands.literal("start").then(
                    Commands.argument("minigame", MinigameArgument.minigame()).executes(this::startMinigame)
                )
            ).then(
                Commands.literal("command")
            )
        )
    }

    private fun listMinigames(context: CommandContext<CommandSourceStack>): Int {
        val formatted = Minigames.all().joinToString("\n") { "ID: ${it.id}, UUID: ${it.uuid}" }
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

    private fun addPlayersToMinigame(
        context: CommandContext<CommandSourceStack>,
        players: Collection<ServerPlayer> = EntityArgument.getPlayers(context, "players")
    ): Int {
        return applyToPlayersInMinigame(
            context,
            players,
            { player, minigame -> minigame.addPlayer(player) },
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
            { player, minigame -> minigame.hasPlayer(player) && minigame.makeSpectator(player) },
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
            { player, minigame -> minigame.hasPlayer(player) && minigame.removeSpectator(player) },
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
            { player, minigame -> minigame.hasPlayer(player) && minigame.makeAdmin(player) },
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
            { player, minigame -> minigame.hasPlayer(player) && minigame.removeAdmin(player) },
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

    private fun otherLeaveMinigame(context: CommandContext<CommandSourceStack>): Int {
        val players = EntityArgument.getPlayers(context, "players")
        return this.removePlayersFromMinigame(players, context)
    }

    private fun removePlayersFromMinigame(players: Collection<ServerPlayer>, context: CommandContext<CommandSourceStack>): Int {
        val total = players.size
        var successes = 0
        for (player in players) {
            val minigame = player.getMinigame()
            if (minigame !== null) {
                minigame.removePlayer(player)
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
        val path = MinigameArgument.InfoPath.getPath(context, "path")
        val info = JsonUtils.GSON.toJson(minigame.getDebugInfo().get(path))
        return context.source.success(info)
    }

    private fun openMinigameSettings(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        return context.source.playerOrException.openMenu(minigame.settings.menu()).commandSuccess()
    }

    private fun getMinigameSetting(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        val name = MinigameArgument.SettingsName.getSettingsName(context, "setting")
        val setting = minigame.settings.get(name) ?: throw INVALID_SETTING_NAME.create()
        return context.source.success("Setting $name for minigame ${minigame.id} is set to ${setting.get()}")
    }

    private fun setMinigameSettingFromOption(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        val name = MinigameArgument.SettingsName.getSettingsName(context, "setting")
        val setting = minigame.settings.get(name) ?: throw INVALID_SETTING_NAME.create()
        val option = MinigameArgument.SettingsOption.getSettingsOption(context, "option")
        val value = setting.getOption(option) ?: throw INVALID_SETTING_OPTION.create()
        setting.setFromOption(option)
        return context.source.success("Setting $name for minigame ${minigame.id} set to option $option ($value)")
    }

    private fun setMinigameSettingFromValue(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        val name = MinigameArgument.SettingsName.getSettingsName(context, "setting")
        val setting = minigame.settings.get(name) ?: throw INVALID_SETTING_NAME.create()
        val value = MinigameArgument.SettingsValue.getSettingsValue(context, "value")
        setting.deserializeAndSet(value)
        return context.source.success("Setting $name for minigame ${minigame.id} set to ${setting.get()}")
    }

    private fun listPlayerTags(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        val player = EntityArgument.getPlayer(context, "player")
        val tags = minigame.tags.get(player).joinToString()
        return context.source.success("Tags for ${player.scoreboard}: ${tags}")
    }

    private fun addPlayerTag(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        val player = EntityArgument.getPlayer(context, "player")
        val tag = ResourceLocationArgument.getId(context, "tag")

        if (!minigame.tags.add(player, tag)) {
            return context.source.fail("${player.scoreboard} already had tag $tag")
        }
        return context.source.success("Successfully added tag $tag to ${player.scoreboard}")
    }

    private fun removePlayerTag(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        val player = EntityArgument.getPlayer(context, "player")
        val tag = ResourceLocationArgument.getId(context, "tag")

        if (!minigame.tags.remove(player, tag)) {
            return context.source.fail("${player.scoreboard} did not have tag $tag")
        }
        return context.source.success("Successfully removed tag $tag for ${player.scoreboard}")
    }

    private fun suggestExistingTags(context: CommandContext<CommandSourceStack>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        val player = EntityArgument.getPlayer(context, "player")
        return SharedSuggestionProvider.suggestResource(minigame.tags.get(player), builder)
    }

    private fun getMinigamePhase(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        return context.source.success("The phase of minigame ${minigame.id} is ${minigame.phase.id}")
    }

    private fun <M: Minigame<M>> setMinigamePhase(context: CommandContext<CommandSourceStack>): Int {
        @Suppress("UNCHECKED_CAST")
        val minigame = MinigameArgument.getMinigame(context, "minigame") as Minigame<M>
        val name = MinigameArgument.PhaseName.getPhaseName(context, "phase")
        val phase = minigame.phases.find { it.id == name } ?: throw INVALID_PHASE_NAME.create()
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
        val callback: () -> Unit = {
            val message = "All players are ready, click ".literal().apply {
                append("[here]".literal().green().suggestCommand("/minigame unpause ${minigame.uuid} countdown 5 Seconds"))
                append(" to start the unpause countdown!")
            }
            minigame.chat.broadcastTo(message, minigame.getAdminPlayers())
        }
        val awaiting = if (teams) {
            val unready = minigame.ui.readier.areTeamsReady(minigame.teams.getPlayingTeams(), callback)
            ({ unready.toComponent() })
        } else {
            val unready = minigame.ui.readier.arePlayersReady(minigame.getPlayingPlayers(), callback)
            ({ unready.toComponent() })
        }
        return context.source.success {
            "Successfully broadcasted unpause ready check click ".literal().apply {
                append("[here]".literal().green().function {
                    val message = "Awaiting the following ${if (teams) "teams" else "players"}: ".literal().append(awaiting())
                    minigame.chat.broadcastTo(message, it.player)
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
        minigame.ui.countdown.countdown(minigame, duration, scheduler = GlobalTickedScheduler.asScheduler())
            .then(minigame::unpause)
        return context.source.success("Successfully started countdown for minigame ${minigame.id}")
    }

    private fun createMinigame(context: CommandContext<CommandSourceStack>): Int {
        val factory = MinigameArgument.Factory.getFactory(context, "factory")
        val minigame = factory.create(MinigameCreationContext(context.source.server))
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