package net.casual.arcade.test.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import eu.pb4.sgui.api.gui.SimpleGui
import net.casual.arcade.commands.CommandTree
import net.casual.arcade.commands.argument
import net.casual.arcade.commands.executes
import net.casual.arcade.commands.literal
import net.casual.arcade.guis.core.ContainerGui
import net.casual.arcade.guis.core.display.GuiItem
import net.casual.arcade.guis.sgui.PlayerInventoryViewGui
import net.casual.arcade.guis.utils.ContainerType
import net.casual.arcade.utils.ItemUtils.named
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.world.inventory.MenuType
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
                executes(::test)
            }
        }
    }

    private fun viewInventory(context: CommandContext<CommandSourceStack>) {
        val player = context.source.playerOrException
        val target = EntityArgument.getPlayer(context, "target")
        val gui = PlayerInventoryViewGui(player, target)
        gui.open()
    }

    private fun test(context: CommandContext<CommandSourceStack>) {
        class FlipFloppingItem: GuiItem {
            private var ticks = 0

            override fun tick() {
                this.ticks++
            }

            override fun display(): ItemStack {
                if ((this.ticks / 20) % 2 == 0) {
                    return ItemStack(Items.DIRT)
                }
                return ItemStack(Items.GRASS_BLOCK)
            }
        }

        val player = context.source.playerOrException
        val gui = ContainerGui(player, ContainerType.Chest, true)
        gui.setSlot(0, FlipFloppingItem())
        gui.setSlot(5, ItemStack(Items.DIAMOND_BLOCK).named("Hello World")) { action ->
            println("$action")
        }
        gui.open()
    }
}