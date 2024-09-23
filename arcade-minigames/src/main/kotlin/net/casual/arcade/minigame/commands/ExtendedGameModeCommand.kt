package net.casual.arcade.minigame.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.casual.arcade.commands.CommandTree
import net.casual.arcade.commands.argument
import net.casual.arcade.commands.arguments.EnumArgument
import net.casual.arcade.commands.requiresPermission
import net.casual.arcade.commands.success
import net.casual.arcade.minigame.gamemode.ExtendedGameMode
import net.casual.arcade.minigame.gamemode.ExtendedGameMode.Companion.extendedGameMode
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

internal object ExtendedGameModeCommand: CommandTree {
    override fun create(buildContext: CommandBuildContext): LiteralArgumentBuilder<CommandSourceStack> {
        return CommandTree.buildLiteral("extended-gamemode") {
            requiresPermission(2)
            argument("gamemode", EnumArgument.enumeration<ExtendedGameMode>()) {
                executes { setGameMode(it, listOf(it.source.playerOrException)) }
                argument("targets", EntityArgument.players()) {
                    executes(::setGameMode)
                }
            }
        }
    }

    private fun setGameMode(
        context: CommandContext<CommandSourceStack>,
        targets: Collection<ServerPlayer> = EntityArgument.getPlayers(context, "targets")
    ): Int {
        val mode = EnumArgument.getEnumeration<ExtendedGameMode>(context, "gamemode")
        for (target in targets) {
            target.extendedGameMode = mode
        }
        return context.source.success(
            Component.translatable("minigame.command.gamemode.success", mode.name)
        )
    }
}