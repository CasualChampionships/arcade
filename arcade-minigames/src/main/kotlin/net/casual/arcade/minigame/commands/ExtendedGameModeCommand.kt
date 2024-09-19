package net.casual.arcade.minigame.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.casual.arcade.commands.*
import net.casual.arcade.commands.arguments.EnumArgument
import net.casual.arcade.minigame.gamemode.ExtendedGameMode
import net.casual.arcade.minigame.gamemode.ExtendedGameMode.Companion.extendedGameMode
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.server.level.ServerPlayer

public object ExtendedGameModeCommand: CommandTree {
    override fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.registerLiteral("extended-gamemode") {
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
        return context.source.success("Successfully updated gamemode to ${mode.name} for targets")
    }
}