package net.casual.arcade.gui.screen

import net.minecraft.world.item.ItemStack

fun interface ItemStackTicker {
    fun tick(stack: ItemStack)
}