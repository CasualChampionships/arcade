package net.casual.arcade.test.command

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.casual.arcade.boundary.LevelBoundary
import net.casual.arcade.boundary.extension.LevelBoundaryExtension.Companion.levelBoundary
import net.casual.arcade.boundary.renderer.AxisAlignedDisplayBoundaryRenderer
import net.casual.arcade.boundary.renderer.options.AxisAlignedModelRenderOptions
import net.casual.arcade.boundary.shape.AxisAlignedBoundaryShape
import net.casual.arcade.commands.*
import net.casual.arcade.utils.TimeUtils.Ticks
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.arguments.TimeArgument
import net.minecraft.commands.arguments.coordinates.Vec3Argument
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3

object LevelBoundaryCommand: CommandTree {
    override fun create(buildContext: CommandBuildContext): LiteralArgumentBuilder<CommandSourceStack> {
        return CommandTree.buildLiteral("boundary") {
            requiresPermission(2)
            literal("create") {
                executes(::createBoundary)
            }
            literal("remove") {
                executes(::removeBoundary)
            }
            literal("resize") {
                argument("size", Vec3Argument.vec3(false)) {
                    executes { resizeBoundary(it, 0) }
                    argument("duration", TimeArgument.time()) {
                        executes(::resizeBoundary)
                    }
                }
            }
            literal("recenter") {
                argument("center", Vec3Argument.vec3()) {
                    executes { recenterBoundary(it, 0) }
                    argument("duration", TimeArgument.time()) {
                        executes(::recenterBoundary)
                    }
                }
            }
            literal("query") {
                executes(::queryBoundary)
            }
        }
    }

    private fun createBoundary(context: CommandContext<CommandSourceStack>): Int {
        val level = context.source.level
        val box = AABB.ofSize(Vec3.ZERO, 100.0, 100.0, 100.0)
        val shape = AxisAlignedBoundaryShape(box)
        shape.recenter(Vec3(0.0, 65.0, 0.0))

        val renderer = AxisAlignedDisplayBoundaryRenderer(shape, AxisAlignedModelRenderOptions.CUBOID_SHADER)
        val boundary = LevelBoundary(shape, renderer)
        level.levelBoundary = boundary
        return context.source.success("Successfully set world boundary")
    }

    private fun removeBoundary(context: CommandContext<CommandSourceStack>): Int {
        val level = context.source.level
        level.levelBoundary = null
        return context.source.success("Successfully removed world boundary")
    }

    private fun resizeBoundary(
        context: CommandContext<CommandSourceStack>,
        durationTicks: Int = IntegerArgumentType.getInteger(context, "duration")
    ): Int {
        val level = context.source.level
        val size = Vec3Argument.getVec3(context, "size")
        val boundary = level.levelBoundary ?: return context.source.fail("World has no boundary set!")
        boundary.shape.resize(size, durationTicks.Ticks)
        if (durationTicks == 0) {
            return context.source.success("Successfully resized boundary")
        }
        return context.source.success("Successfully resizing boundary")
    }

    private fun recenterBoundary(
        context: CommandContext<CommandSourceStack>,
        durationTicks: Int = IntegerArgumentType.getInteger(context, "duration")
    ): Int {
        val level = context.source.level
        val center = Vec3Argument.getVec3(context, "center")
        val boundary = level.levelBoundary ?: return context.source.fail("World has no boundary set!")
        boundary.shape.recenter(center, durationTicks.Ticks)
        if (durationTicks == 0) {
            return context.source.success("Successfully re-centered boundary")
        }
        return context.source.success("Successfully re-centering boundary")
    }

    private fun queryBoundary(context: CommandContext<CommandSourceStack>): Int {
        val boundary = context.source.level.levelBoundary ?: return context.source.fail("World has no boundary set!")
        return context.source.success("Boundary center: ${boundary.getCenter()}, size: ${boundary.getSize()}")
    }
}