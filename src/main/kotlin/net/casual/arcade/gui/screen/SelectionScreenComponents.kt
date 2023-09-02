package net.casual.arcade.gui.screen

import net.casual.arcade.utils.ItemUtils.literalNamed
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

interface SelectionScreenComponents {
    fun getTitle(): Component {
        return Component.literal("Selection Screen")
    }

    fun getPrevious(): ItemStack {
        return ItemStack(Items.RED_STAINED_GLASS).literalNamed("Previous")
    }

    fun getBack(): ItemStack {
        return ItemStack(Items.RED_STAINED_GLASS).literalNamed("Back")
    }

    fun getNext(): ItemStack {
        return ItemStack(Items.GREEN_STAINED_GLASS).literalNamed("Next")
    }

    fun getFiller(): ItemStack {
        return ItemStack(Items.GRAY_STAINED_GLASS).literalNamed("")
    }

    companion object {
        val DEFAULT = object: SelectionScreenComponents { }
    }
}