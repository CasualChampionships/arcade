package net.casual.arcade.minigame.commands

import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.casual.arcade.commands.*
import net.casual.arcade.utils.ComponentUtils.joinToComponent
import net.casual.arcade.utils.TeamUtils
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.network.chat.Component
import net.minecraft.world.scores.Team.CollisionRule.ALWAYS

internal object TeamCommandModifier: CommandTree {
    override fun create(buildContext: CommandBuildContext): LiteralArgumentBuilder<CommandSourceStack> {
        return CommandTree.buildLiteral<CommandSourceStack>("team").literal("randomize") {
            literal("with") {
                argument("players", EntityArgument.entities()) {
                    argument("size", IntegerArgumentType.integer(1)) {
                        argument("friendly-fire", BoolArgumentType.bool()) {
                            executes(::createRandomTeams)
                        }
                        executes { createRandomTeams(it, false) }
                    }
                }
            }
            literal("delete").executes(::deleteRandomTeams)
        }
    }

    private fun createRandomTeams(
        context: CommandContext<CommandSourceStack>,
        friendlyFire: Boolean = BoolArgumentType.getBool(context, "friendly-fire"),
    ): Int {
        val players = EntityArgument.getPlayers(context, "players")
        val size = IntegerArgumentType.getInteger(context, "size")
        val teams = TeamUtils.createRandomTeams(context.source.server, players, size, friendlyFire, ALWAYS)
            ?: return context.source.fail(Component.translatable("minigame.command.team.randomizer.fail"))

        val generated = teams.joinToComponent { it.formattedDisplayName }
        return context.source.success(
            Component.translatable("minigame.command.team.randomizer.success", generated), true
        )
    }

    private fun deleteRandomTeams(context: CommandContext<CommandSourceStack>): Int {
        TeamUtils.deleteAllRandomTeams(context.source.server.scoreboard)
        return context.source.success(Component.translatable("minigame.command.team.randomizer.deleted"))
    }
}