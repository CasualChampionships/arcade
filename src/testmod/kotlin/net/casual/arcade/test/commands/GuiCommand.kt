package net.casual.arcade.test.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.casual.arcade.commands.CommandTree
import net.casual.arcade.commands.argument
import net.casual.arcade.commands.executes
import net.casual.arcade.commands.literal
import net.casual.arcade.guis.core.ContainerGui
import net.casual.arcade.guis.core.display.GuiItem
import net.casual.arcade.guis.sgui.PlayerInventoryViewGui
import net.casual.arcade.guis.utils.ContainerType
import net.casual.arcade.test.guis.TestingGui
import net.casual.arcade.utils.ItemUtils.named
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

@Suppress("unused")
object GuiCommand: CommandTree<CommandSourceStack> {
    override fun create(buildContext: CommandBuildContext): LiteralArgumentBuilder<CommandSourceStack> {
        return CommandTree.buildLiteral("gui") {
            literal("view-inventory") {
                argument("target", EntityArgument.player()) {
                    executes(::viewInventory)
                }
            }
            literal("test") {
                executes(::openTestingGui)
            }
        }
    }

    private fun viewInventory(context: CommandContext<CommandSourceStack>) {
        val player = context.source.playerOrException
        val target = EntityArgument.getPlayer(context, "target")
        val gui = PlayerInventoryViewGui(player, target)
        gui.open()
    }

    private fun openTestingGui(context: CommandContext<CommandSourceStack>) {
        val player = context.source.playerOrException
        TestingGui(player).open()
    }
}