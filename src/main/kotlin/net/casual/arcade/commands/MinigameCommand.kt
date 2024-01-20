package net.casual.arcade.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.casual.arcade.commands.arguments.MinigameArgument
import net.casual.arcade.commands.arguments.MinigameArgument.PhaseName.Companion.INVALID_PHASE_NAME
import net.casual.arcade.commands.arguments.MinigameArgument.SettingsName.Companion.INVALID_SETTING_NAME
import net.casual.arcade.commands.arguments.MinigameArgument.SettingsOption.Companion.INVALID_SETTING_OPTION
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.Minigames
import net.casual.arcade.minigame.serialization.MinigameCreationContext
import net.casual.arcade.utils.CommandUtils.commandSuccess
import net.casual.arcade.utils.CommandUtils.fail
import net.casual.arcade.utils.CommandUtils.success
import net.casual.arcade.utils.JsonUtils
import net.casual.arcade.utils.MinigameUtils.getMinigame
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.TeamArgument
import net.minecraft.server.level.ServerPlayer

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
                    Commands.argument("minigame", MinigameArgument.minigame()).executes(this::unpauseMinigame)
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
        minigame.pause()
        return context.source.success("Successfully paused minigame ${minigame.id}")
    }

    private fun unpauseMinigame(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        minigame.unpause()
        return context.source.success("Successfully unpaused minigame ${minigame.id}")
    }

    private fun createMinigame(context: CommandContext<CommandSourceStack>): Int {
        val factory = MinigameArgument.Factory.getFactory(context, "factory")
        val minigame = factory.create(MinigameCreationContext(context.source.server))
        return context.source.success("Successfully created minigame ${minigame.id} with uuid ${minigame.uuid}")
    }

    private fun closeMinigame(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        minigame.close()
        return context.source.success("Successfully closed minigame ${minigame.id}")
    }
}