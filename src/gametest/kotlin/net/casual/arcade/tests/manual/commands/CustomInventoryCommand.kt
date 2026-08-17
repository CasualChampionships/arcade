package net.casual.arcade.tests.manual.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.casual.arcade.commands.CommandTree
import net.casual.arcade.commands.executes
import net.casual.arcade.commands.literal
import net.casual.arcade.guis.utils.SlotInteractAction
import net.casual.arcade.guis.inventory.VirtualInventory
import net.casual.arcade.guis.utils.setCustomInventory
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

@Suppress("unused")
object CustomInventoryCommand: CommandTree<CommandSourceStack> {
    override fun create(buildContext: CommandBuildContext): LiteralArgumentBuilder<CommandSourceStack> {
        return CommandTree.buildLiteral("custom-inventory") {
            literal("hotbar") {
                executes(::setCustomHotbarInventory)
            }
        }
    }

    private fun setCustomHotbarInventory(context: CommandContext<CommandSourceStack>) {
        val player = context.source.playerOrException
        val inventory = VirtualInventory(player)
        inventory.setSlot(2, ItemStack(Items.GOLD_ORE))
        inventory.setSlot(3, ItemStack(Items.DIAMOND_ORE)) { action ->
            if (action == SlotInteractAction.Swing) {
                println("Swung diamond ore")
            }
            true
        }
        inventory.setDefaultInteractHandler { _ ->
            false
        }
        player.setCustomInventory(inventory)
    }
}