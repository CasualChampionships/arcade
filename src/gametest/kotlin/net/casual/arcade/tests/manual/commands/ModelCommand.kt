package net.casual.arcade.tests.manual.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.casual.arcade.commands.CommandTree
import net.casual.arcade.commands.argument
import net.casual.arcade.commands.executes
import net.casual.arcade.commands.fail
import net.casual.arcade.commands.literal
import net.casual.arcade.commands.location
import net.casual.arcade.commands.success
import net.casual.arcade.commands.suggests
import net.casual.arcade.model.definition.ModelDefinition
import net.casual.arcade.model.format.blockbench.BlockbenchModelLoader
import net.casual.arcade.model.pack.addModel
import net.casual.arcade.model.virtual.ModelVirtualEntity
import net.casual.arcade.observer.tracker.SimpleObserverTracker
import net.casual.arcade.pack.generation.PackDefinition
import net.casual.arcade.tests.manual.ArcadeTest
import net.casual.arcade.tests.manual.resource_pack.TestResourcePacks
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.arcade
import net.casual.arcade.virtual.entity.attachment.SimpleVirtualEntityAttachment
import net.casual.arcade.virtual.entity.location.VirtualPosition
import net.casual.arcade.virtual.entity.location.VirtualRotation
import net.casual.arcade.virtual.entity.utils.attach
import net.casual.arcade.virtual.entity.utils.createVirtualEntityAttachment
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.arguments.IdentifierArgument
import net.minecraft.resources.Identifier
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.nameWithoutExtension

@Suppress("unused")
object ModelCommand: CommandTree<CommandSourceStack> {
    private val definitions = HashMap<Identifier, ModelDefinition>()
    private val pack = PackDefinition("model-engine") {
        for (definition in definitions.values) {
            addModel(definition)
        }
    }

    init {
        val models = ArcadeTest.container.findPath("models").get()
        for (model in models.listDirectoryEntries("*.bbmodel")) {
            val id = arcade(model.nameWithoutExtension)
            this.definitions[id] = BlockbenchModelLoader.load(id, model)
        }

        TestResourcePacks.register("model-engine", this.pack)
    }

    override fun create(buildContext: CommandBuildContext): LiteralArgumentBuilder<CommandSourceStack> {
        return CommandTree.buildLiteral("model") {
            literal("spawn") {
                argument("id", IdentifierArgument.id()) {
                    suggests(definitions.keys.map(Identifier::toString))
                    executes(::spawnTestModel)
                }
            }
        }
    }

    private fun spawnTestModel(context: CommandContext<CommandSourceStack>): Int {
        val level = context.source.level
        val location = context.source.location

        val model = IdentifierArgument.getId(context, "id")
        val definition =this.definitions[model] ?: return context.source.fail("No such model with id '$model'")

        val attachment = level.createVirtualEntityAttachment(::SimpleVirtualEntityAttachment)
        val entity = attachment.attach { ModelVirtualEntity(definition, it, SimpleObserverTracker()) }
        entity.position = VirtualPosition.Absolute(location.position)
        entity.rotation = VirtualRotation.Absolute(location.rotation)
        return context.source.success("Successfully spawned model '$model'")
    }
}