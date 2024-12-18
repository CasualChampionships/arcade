/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.border.command

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.casual.arcade.border.CustomBorder
import net.casual.arcade.commands.CommandTree
import net.casual.arcade.commands.argument
import net.casual.arcade.commands.arguments.EnumArgument
import net.casual.arcade.commands.literal
import net.casual.arcade.commands.success
import net.casual.arcade.utils.time.MinecraftTimeUnit
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.arguments.coordinates.Vec2Argument
import net.minecraft.network.chat.Component
import net.minecraft.world.level.border.WorldBorder
import kotlin.math.abs

internal object WorldBorderCommandModifier: CommandTree {
    private val CANNOT_LERP_CENTER = SimpleCommandExceptionType(Component.translatable("commands.worldborder.set.center.unsupported"))
    private val ERROR_TOO_FAR_OUT = SimpleCommandExceptionType(Component.translatable("commands.worldborder.set.failed.far", 2.9999984E7))

    override fun create(buildContext: CommandBuildContext): LiteralArgumentBuilder<CommandSourceStack> {
        return CommandTree.buildLiteral("worldborder") {
            literal("center") {
                argument("pos", Vec2Argument.vec2()) {
                    argument("time", IntegerArgumentType.integer(0)) {
                        argument("unit", EnumArgument.enumeration<MinecraftTimeUnit>()) {
                            executes(WorldBorderCommandModifier::lerpCenter)
                        }
                    }
                }
            }
        }
    }

    private fun lerpCenter(context: CommandContext<CommandSourceStack>): Int {
        val border = context.source.level.worldBorder
        if (border !is CustomBorder) {
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
}