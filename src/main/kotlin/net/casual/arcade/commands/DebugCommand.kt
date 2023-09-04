package net.casual.arcade.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.DoubleArgumentType
import net.casual.arcade.border.ArcadeBorder
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component


object DebugCommand: Command {
    override fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("arcadeDebug").requires {
                it.hasPermission(4)
            }.then(Commands.literal("border")
                .then(Commands.literal("set-center")
                    .then(Commands.argument("posX", DoubleArgumentType.doubleArg())
                    .then(Commands.argument("posZ", DoubleArgumentType.doubleArg())
                .executes {
                    val posX = DoubleArgumentType.getDouble(it, "posX")
                    val posZ = DoubleArgumentType.getDouble(it, "posY")
                    if (it.source.level.worldBorder is ArcadeBorder) {
                    (it.source.level.worldBorder as ArcadeBorder).setCenter(posX, posZ)
                    } else {
                        it.source.sendSystemMessage(Component.literal("Border is not an ArcadeBorder, setting normal center!").withStyle(ChatFormatting.DARK_RED))
                        it.source.level.worldBorder.setCenter(posX, posZ)
                    }

                    it.source.sendSuccess({ Component.literal("WorldBorder Set!") }, true)

                    return@executes 1
                }
        )))))
    }
}