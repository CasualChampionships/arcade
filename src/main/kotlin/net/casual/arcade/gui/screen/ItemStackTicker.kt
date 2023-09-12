package net.casual.arcade.gui.screen

import net.minecraft.world.item.ItemStack

/**
 * This interface provides a method to tick an [ItemStack].
 */
fun interface ItemStackTicker {
    /**
     * This ticks a [stack].
     *
     * @param stack The [ItemStack] instance.
     */
    fun tick(stack: ItemStack)
}