package net.casual.arcade.test.command

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.casual.arcade.commands.CommandTree
import net.casual.arcade.commands.argument
import net.casual.arcade.commands.executes
import net.casual.arcade.commands.literal
import net.casual.arcade.guis.sgui.PlayerInventoryViewGui
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.arguments.EntityArgument

object GuiTestCommand: CommandTree {
    override fun create(buildContext: CommandBuildContext): LiteralArgumentBuilder<CommandSourceStack> {
        return CommandTree.buildLiteral("gui-test") {
            literal("view-inventory") {
                argument("target", EntityArgument.player()) {
                    executes(::viewInventory)
                }
            }
        }
    }

    private fun viewInventory(context: CommandContext<CommandSourceStack>) {
        val player = context.source.playerOrException
        val target = EntityArgument.getPlayer(context, "target")
        val gui = PlayerInventoryViewGui(player, target)
        gui.open()
    }
}