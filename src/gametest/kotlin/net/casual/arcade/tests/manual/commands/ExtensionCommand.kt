package net.casual.arcade.tests.manual.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.casual.arcade.commands.CommandTree
import net.casual.arcade.commands.argument
import net.casual.arcade.commands.literal
import net.casual.arcade.commands.success
import net.casual.arcade.extensions.utils.getExtension
import net.casual.arcade.tests.manual.extensions.TestEntityExtension
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.arguments.EntityArgument

@Suppress("unused")
object ExtensionCommand: CommandTree<CommandSourceStack> {
    fun registerEvents() {
        TestEntityExtension.registerEvents()
    }

    override fun create(buildContext: CommandBuildContext): LiteralArgumentBuilder<CommandSourceStack> {
        return CommandTree.buildLiteral("extension") {
            literal("entity") {
                literal("get") {
                    argument("entity", EntityArgument.entity()) {
                        executes(::getExtensionValue)
                    }
                }
            }
        }
    }

    private fun getExtensionValue(context: CommandContext<CommandSourceStack>): Int {
        val entity = EntityArgument.getEntity(context, "entity")
        val extension = entity.getExtension<TestEntityExtension>()
        return context.source.success("Entity's value: ${extension.value}")
    }
}