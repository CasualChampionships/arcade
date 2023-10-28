package net.casual.arcade.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.context.CommandContext
import net.casual.arcade.commands.arguments.EnumArgument
import net.casual.arcade.utils.CommandUtils.success
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.TeamUtils
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.world.scores.Team.CollisionRule
import net.minecraft.world.scores.Team.CollisionRule.ALWAYS

internal object TeamCommandModifier: Command {
    override fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("team").then(
                Commands.literal("randomise").then(
                    Commands.argument("players", EntityArgument.players()).then(
                        Commands.argument("size", IntegerArgumentType.integer(1)).then(
                            Commands.argument("friendlyFire", BoolArgumentType.bool()).then(
                                Commands.argument("collision", EnumArgument.enumeration<CollisionRule>()).executes(this::createRandomTeams)
                            ).executes { this.createRandomTeams(it, collision = ALWAYS) }
                        ).executes { this.createRandomTeams(it, friendlyFire = false, collision = ALWAYS) }
                    )
                ).then(
                    Commands.literal("delete").executes(this::deleteRandomTeams)
                )
            )
        )
    }

    private fun createRandomTeams(
        context: CommandContext<CommandSourceStack>,
        friendlyFire: Boolean = BoolArgumentType.getBool(context, "friendlyFire"),
        collision: CollisionRule = EnumArgument.getEnumeration(context, "collision")
    ): Int {
        val players = EntityArgument.getPlayers(context, "players")
        val size = IntegerArgumentType.getInteger(context, "size")
        val teams = TeamUtils.createRandomTeams(context.source.server, players, size, friendlyFire, collision)
        val component = "Successfully created the following teams: ".literal()
        for (team in teams) {
            if (component.siblings.isNotEmpty()) {
                component.append(", ")
            }
            component.append(team.formattedDisplayName)
        }
        return context.source.success(component, true)
    }

    private fun deleteRandomTeams(context: CommandContext<CommandSourceStack>): Int {
        TeamUtils.deleteAllRandomTeams(context.source.server.scoreboard)
        return context.source.success("Successfully deleted all random teams")
    }
}