package net.casual.arcade.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.casual.arcade.commands.arguments.MinigameArgument
import net.casual.arcade.commands.arguments.MinigameArgument.SettingsName.Companion.INVALID_SETTING_NAME
import net.casual.arcade.commands.arguments.MinigameArgument.SettingsOption.Companion.INVALID_SETTING_OPTION
import net.casual.arcade.utils.CommandUtils.commandSuccess
import net.casual.arcade.utils.CommandUtils.fail
import net.casual.arcade.utils.CommandUtils.success
import net.casual.arcade.utils.MinigameUtils.getMinigame
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

object MinigameCommand: Command {
    override fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("minigame").requires {
                it.hasPermission(4)
            }.then(
                Commands.literal("join").then(
                    Commands.argument("minigame", MinigameArgument.minigame()).then(
                        Commands.argument("players", EntityArgument.players()).executes(this::otherJoinMinigame)
                    ).executes(this::selfJoinMinigame)
                )
            ).then(
                Commands.literal("leave").then(
                    Commands.argument("players", EntityArgument.players()).executes(this::otherLeaveMinigame)
                ).executes(this::selfLeaveMinigame)
            ).then(
                Commands.literal("info").then(
                    Commands.argument("minigame", MinigameArgument.minigame()).executes(this::infoMinigame)
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
            )
        )
    }

    private fun selfJoinMinigame(context: CommandContext<CommandSourceStack>): Int {
        return this.addPlayersToMinigame(listOf(context.source.playerOrException), context)
    }

    private fun otherJoinMinigame(context: CommandContext<CommandSourceStack>): Int {
        val players = EntityArgument.getPlayers(context, "players")
        return this.addPlayersToMinigame(players, context)
    }

    private fun addPlayersToMinigame(players: Collection<ServerPlayer>, context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        val total = players.size
        var successes = 0
        for (player in players) {
            if (minigame.addPlayer(player)) {
                successes++
            }
        }
        if (successes == 0) {
            return context.source.fail(Component.literal("Failed to add any players to minigame"))
        }
        context.source.success(Component.literal("Successfully added $successes/$total players to minigame"))
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
            return context.source.fail(Component.literal("Failed to remove any players from minigames"))
        }
        context.source.success(Component.literal("Successfully removed $successes/$total players from minigames"))
        return successes
    }

    private fun infoMinigame(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        return context.source.success(Component.literal(minigame.toString()))
    }

    private fun openMinigameSettings(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        return context.source.playerOrException.openMenu(minigame.createRulesMenu()).commandSuccess()
    }

    private fun getMinigameSetting(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        val name = MinigameArgument.SettingsName.getSettingsName(context, "setting")
        val setting = minigame.getSetting(name) ?: throw INVALID_SETTING_NAME.create()
        return context.source.success(Component.literal("Setting $name for minigame ${minigame.id} is set to ${setting.get()}"))
    }

    private fun setMinigameSettingFromOption(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        val name = MinigameArgument.SettingsName.getSettingsName(context, "setting")
        val setting = minigame.getSetting(name) ?: throw INVALID_SETTING_NAME.create()
        val option = MinigameArgument.SettingsOption.getSettingsOption(context, "option")
        val value = setting.getOption(option) ?: throw INVALID_SETTING_OPTION.create()
        setting.setFromOption(option)
        return context.source.success(Component.literal("Setting $name for minigame ${minigame.id} set to option $option ($value)"))
    }

    private fun setMinigameSettingFromValue(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        val name = MinigameArgument.SettingsName.getSettingsName(context, "setting")
        val setting = minigame.getSetting(name) ?: throw INVALID_SETTING_NAME.create()
        val value = MinigameArgument.SettingsValue.getSettingsValue(context, "value")
        setting.deserializeAndSet(value)
        return context.source.success(Component.literal("Setting $name for minigame ${minigame.id} set to ${setting.get()}"))
    }
}