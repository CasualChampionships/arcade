package net.casual.arcade.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.context.CommandContext
import net.casual.arcade.border.ArcadeBorder
import net.casual.arcade.utils.CommandUtils.fail
import net.casual.arcade.utils.CommandUtils.success
import net.casual.arcade.utils.ComponentUtils.red
import net.casual.arcade.utils.TimeUtils.Seconds
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component

internal object DebugCommand: Command {
    override fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("arcade").requires {
                it.hasPermission(4)
            }.then(
                Commands.literal("border").then(
                    Commands.literal("center").then(
                        Commands.argument("posX", DoubleArgumentType.doubleArg()).then(
                            Commands.argument("posZ", DoubleArgumentType.doubleArg()).then(
                                Commands.argument("seconds", IntegerArgumentType.integer(0)).executes(this::setBorderCenter)
                            )
                        )
                    )
                )
            )
        )
    }

    private fun setBorderCenter(context: CommandContext<CommandSourceStack>): Int {
        val posX = DoubleArgumentType.getDouble(context, "posX")
        val posZ = DoubleArgumentType.getDouble(context, "posZ")
        val seconds = IntegerArgumentType.getInteger(context, "seconds")
        val border = context.source.level.worldBorder
        if (border is ArcadeBorder) {
            border.lerpCenterTo(posX, posZ, seconds.Seconds)
            return context.source.success("Border set", true)
        }
        return context.source.fail("Border is not ArcadeBorder")
    }
}