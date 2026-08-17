package net.casual.arcade.tests.manual.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.casual.arcade.commands.CommandTree
import net.casual.arcade.commands.argument
import net.casual.arcade.commands.executes
import net.casual.arcade.commands.literal
import net.casual.arcade.guis.presets.PlayerInventoryViewGui
import net.casual.arcade.tests.manual.guis.TestBookGui
import net.casual.arcade.tests.manual.guis.TestContainerGui
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.arguments.EntityArgument

@Suppress("unused")
object GuiCommand: CommandTree<CommandSourceStack> {
    override fun create(buildContext: CommandBuildContext): LiteralArgumentBuilder<CommandSourceStack> {
        return CommandTree.buildLiteral("gui") {
            literal("view-inventory") {
                argument("target", EntityArgument.player()) {
                    executes(::viewInventory)
                }
            }
            literal("test-container") {
                executes(::openTestContainerGui)
            }
            literal("test-book") {
                executes(::openTestBookGui)
            }
        }
    }

    private fun viewInventory(context: CommandContext<CommandSourceStack>) {
        val player = context.source.playerOrException
        val target = EntityArgument.getPlayer(context, "target")
        PlayerInventoryViewGui(player, target).open()
    }

    private fun openTestContainerGui(context: CommandContext<CommandSourceStack>) {
        val player = context.source.playerOrException
        TestContainerGui(player).open()
    }

    private fun openTestBookGui(context: CommandContext<CommandSourceStack>) {
        val player = context.source.playerOrException
        TestBookGui(player).open()
    }
}