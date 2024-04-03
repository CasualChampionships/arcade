package net.casual.arcade.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.casual.arcade.border.ArcadeBorder
import net.casual.arcade.commands.arguments.EnumArgument
import net.casual.arcade.scheduler.MinecraftTimeUnit
import net.casual.arcade.utils.BorderUtils
import net.casual.arcade.utils.CommandUtils.fail
import net.casual.arcade.utils.CommandUtils.success
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.DimensionArgument
import net.minecraft.commands.arguments.coordinates.Vec2Argument
import net.minecraft.network.chat.Component
import net.minecraft.world.level.border.WorldBorder
import kotlin.math.abs

public object WorldBorderCommandModifier: Command {
    private val CANNOT_LERP_CENTER = SimpleCommandExceptionType(Component.literal("World border doesn't support moving center"))
    private val ERROR_TOO_FAR_OUT = SimpleCommandExceptionType(Component.translatable("commands.worldborder.set.failed.far", 2.9999984E7))

    override fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("worldborder").then(
                Commands.literal("center").then(
                    Commands.argument("pos", Vec2Argument.vec2()).then(
                        Commands.argument("time", IntegerArgumentType.integer(0)).then(
                            Commands.argument("unit", EnumArgument.enumeration<MinecraftTimeUnit>()).executes(this::lerpCenter)
                        )
                    )
                )
            ).then(
                Commands.literal("separate").executes(this::separateBorders)
            ).then(
                Commands.literal("join").executes(this::joinBorders)
            )
        )
    }

    private fun lerpCenter(context: CommandContext<CommandSourceStack>): Int {
        val border = context.source.level.worldBorder
        if (border !is ArcadeBorder) {
            throw CANNOT_LERP_CENTER.create()
        }
        val pos = Vec2Argument.getVec2(context, "pos")
        if (abs(pos.x) > WorldBorder.MAX_CENTER_COORDINATE || abs(pos.y) > WorldBorder.MAX_CENTER_COORDINATE) {
            throw ERROR_TOO_FAR_OUT.create()
        }
        val time = IntegerArgumentType.getInteger(context, "time")
        val unit = EnumArgument.getEnumeration<MinecraftTimeUnit>(context, "unit")
        return if (time == 0) {
            border.changeCenter(pos.x.toDouble(), pos.y.toDouble())
            context.source.success("Successfully moved center to ${pos.x}, ${pos.y}")
        } else {
            border.lerpCenterTo(pos.x.toDouble(), pos.y.toDouble(), unit.duration(time))
            context.source.success("Successfully moving center to ${pos.x}, ${pos.y}")
        }
    }

    private fun separateBorders(context: CommandContext<CommandSourceStack>): Int {
        if (BorderUtils.isolateWorldBorders()) {
            return context.source.success("Successfully separated borders")
        }
        return context.source.fail("Borders were already separated")
    }

    private fun joinBorders(context: CommandContext<CommandSourceStack>): Int {
        if (BorderUtils.joinWorldBorders()) {
            return context.source.success("Successfully joined borders")
        }
        return context.source.fail("Borders were already joined")
    }
}