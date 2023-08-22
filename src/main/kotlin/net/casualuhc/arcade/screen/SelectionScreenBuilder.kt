package net.casualuhc.arcade.screen

import net.casualuhc.arcade.screen.SelectionScreen.Selection
import net.casualuhc.arcade.utils.ItemUtils.literalNamed
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.MenuProvider
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

class SelectionScreenBuilder {
    private val selections = ArrayList<Selection>()

    var title: Component = Component.literal("Selection Screen")
    var previous: ItemStack = ItemStack(Items.RED_STAINED_GLASS).literalNamed("Previous")
    var next: ItemStack = ItemStack(Items.GREEN_STAINED_GLASS).literalNamed("Next")
    var filler: ItemStack = ItemStack(Items.GRAY_STAINED_GLASS).literalNamed("")

    fun title(component: Component): SelectionScreenBuilder {
        this.title = component
        return this
    }

    fun previous(stack: ItemStack): SelectionScreenBuilder {
        this.previous = stack
        return this
    }

    fun next(stack: ItemStack): SelectionScreenBuilder {
        this.next = stack
        return this
    }

    fun filler(stack: ItemStack): SelectionScreenBuilder {
        this.filler = stack
        return this
    }

    fun selection(stack: ItemStack, action: (ServerPlayer) -> Unit): SelectionScreenBuilder {
        this.selections.add(Selection(stack.copy(), action))
        return this
    }

    fun build(): MenuProvider {
        return SelectionScreen.createScreenFactory(
            this.title,
            this.selections,
            0,
            this.previous,
            this.next,
            this.filler
        )!!
    }
}