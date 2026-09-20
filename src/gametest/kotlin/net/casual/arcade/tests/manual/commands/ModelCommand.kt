package net.casual.arcade.tests.manual.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.casual.arcade.commands.CommandTree
import net.casual.arcade.commands.executes
import net.casual.arcade.commands.literal
import net.casual.arcade.commands.location
import net.casual.arcade.model.format.blockbench.BlockbenchModelLoader
import net.casual.arcade.model.virtual.ModelVirtualEntity
import net.casual.arcade.observer.tracker.SimpleObserverTracker
import net.casual.arcade.tests.manual.ArcadeTest
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.arcade
import net.casual.arcade.virtual.entity.attachment.SimpleVirtualEntityAttachment
import net.casual.arcade.virtual.entity.location.VirtualPosition
import net.casual.arcade.virtual.entity.location.VirtualRotation
import net.casual.arcade.virtual.entity.utils.attach
import net.casual.arcade.virtual.entity.utils.createVirtualEntityAttachment
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack

@Suppress("unused")
object ModelCommand: CommandTree<CommandSourceStack> {
    override fun create(buildContext: CommandBuildContext): LiteralArgumentBuilder<CommandSourceStack> {
        return CommandTree.buildLiteral("model") {
            literal("spawn") {
                executes(::spawnTestModel)
            }
        }
    }

    private fun spawnTestModel(context: CommandContext<CommandSourceStack>) {
        val level = context.source.level
        val location = context.source.location

        val path = ArcadeTest.container.findPath("models/pigeon.bbmodel").get()
        val definition = try {
            BlockbenchModelLoader.load(arcade("test"), path)
        } catch (e: Exception) {
            ArcadeUtils.logger.error("Failed to load model", e)
            return
        }

        val attachment = level.createVirtualEntityAttachment(::SimpleVirtualEntityAttachment)
        val entity = attachment.attach { ModelVirtualEntity(definition, it, SimpleObserverTracker()) }
        entity.position = VirtualPosition.Absolute(location.position)
        entity.rotation = VirtualRotation.Absolute(location.rotation)
    }
}