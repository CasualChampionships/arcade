package net.casual.arcade.items

import net.minecraft.world.item.ItemStack

/**
 * Functional interface for creating [ItemStack] instances.
 */
fun interface ItemStackFactory {
    /**
     * Creates an [ItemStack] instance.
     *
     * @return The stack instance.
     */
    fun create(): ItemStack
}