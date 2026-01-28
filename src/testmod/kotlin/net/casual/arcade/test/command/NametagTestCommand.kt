package net.casual.arcade.test.command

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.casual.arcade.commands.CommandTree
import net.casual.arcade.commands.argument
import net.casual.arcade.commands.executes
import net.casual.arcade.nametags.Nametag
import net.casual.arcade.nametags.extensions.EntityNametagExtension.Companion.nametagExtension
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.arguments.ComponentArgument
import net.minecraft.commands.arguments.EntityArgument

object NametagTestCommand: CommandTree {
    override fun create(buildContext: CommandBuildContext): LiteralArgumentBuilder<CommandSourceStack> {
        return CommandTree.buildLiteral("nametag-test") {
            argument("targets", EntityArgument.entities()) {
                argument("text", ComponentArgument.textComponent(buildContext)) {
                    executes(::addNametag)
                }
            }
        }
    }

    private fun addNametag(context: CommandContext<CommandSourceStack>) {
        val targets = EntityArgument.getEntities(context, "targets")
        for (entity in targets) {
            val text = ComponentArgument.getResolvedComponent(context, "text", entity)
            entity.nametagExtension.add(Nametag.simple(text))
        }
    }
}