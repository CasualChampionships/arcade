package net.casual.arcade.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.casual.arcade.entity.player.ExtendedGameMode
import net.casual.arcade.entity.player.ExtendedGameMode.Companion.extendedGameMode
import net.casual.arcade.utils.CommandUtils.argument
import net.casual.arcade.utils.CommandUtils.literal
import net.casual.arcade.utils.CommandUtils.registerLiteral
import net.casual.arcade.utils.CommandUtils.requiresPermission
import net.casual.arcade.utils.CommandUtils.success
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.server.level.ServerPlayer

public object ExtendedGameModeCommand: Command {
    override fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.registerLiteral("extended-gamemode") {
            requiresPermission(2)
            for (mode in ExtendedGameMode.entries) {
                literal(mode.name) {
                    executes { setGameMode(it, mode, listOf(it.source.playerOrException)) }
                    argument("targets", EntityArgument.players()) {
                        executes { setGameMode(it, mode) }
                    }
                }
            }
        }
    }

    private fun setGameMode(
        context: CommandContext<CommandSourceStack>,
        mode: ExtendedGameMode,
        targets: Collection<ServerPlayer> = EntityArgument.getPlayers(context, "targets")
    ): Int {
        for (target in targets) {
            target.extendedGameMode = mode
        }
        return context.source.success("Successfully updated gamemode to ${mode.name} for targets")
    }
}