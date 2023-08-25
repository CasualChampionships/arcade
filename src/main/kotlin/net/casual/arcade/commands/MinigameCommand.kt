package net.casual.arcade.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.Minigames
import net.casual.arcade.utils.CommandSourceUtils.fail
import net.casual.arcade.utils.CommandSourceUtils.success
import net.casual.arcade.utils.MinigameUtils.getMinigame
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import java.util.*

object MinigameCommand: Command {
    private val INVALID_UUID = SimpleCommandExceptionType(Component.literal("Invalid Minigame UUID"))

    override fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("minigame").requires {
                it.hasPermission(4)
            }.then(
                Commands.literal("join").then(
                    Commands.argument("uuid", StringArgumentType.word()).suggests { _, builder ->
                        SharedSuggestionProvider.suggest(Minigames.all().stream().map { it.uuid.toString() }, builder)
                    }.then(
                        Commands.argument("players", EntityArgument.players()).executes(this::otherJoinMinigame)
                    ).executes(this::selfJoinMinigame)
                )
            ).then(
                Commands.literal("leave").then(
                    Commands.argument("players", EntityArgument.players()).executes(this::otherLeaveMinigame)
                ).executes(this::selfLeaveMinigame)
            ).then(
                Commands.literal("info").then(
                    Commands.argument("uuid", StringArgumentType.word()).suggests { _, builder ->
                        SharedSuggestionProvider.suggest(Minigames.all().stream().map { it.uuid.toString() }, builder)
                    }.executes(this::selfJoinMinigame)
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
        val minigame = this.getMinigameFromContextUUID(context)
        val total = players.size
        var successes = 0
        for (player in players) {
            if (minigame.addPlayer(player)) {
                successes++
            }
        }
        if (successes == 0) {
            context.source.fail(Component.literal("Failed to add any players to minigame"))
            return 0
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
            context.source.fail(Component.literal("Failed to remove any players from minigames"))
            return 0
        }
        context.source.success(Component.literal("Successfully removed $successes/$total players from minigames"))
        return successes
    }

    private fun infoMinigame(context: CommandContext<CommandSourceStack>) {
        val minigame = this.getMinigameFromContextUUID(context)
        context.source.success(Component.literal(minigame.toString()))
    }

    private fun getMinigameFromContextUUID(context: CommandContext<CommandSourceStack>): Minigame {
        val uuid: UUID
        try {
            uuid = UUID.fromString(StringArgumentType.getString(context, "uuid"))
        } catch (e: IllegalArgumentException) {
            throw INVALID_UUID.create()
        }
        return Minigames.get(uuid) ?: throw INVALID_UUID.create()
    }
}