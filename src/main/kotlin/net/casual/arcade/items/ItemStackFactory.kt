package net.casual.arcade.items

import net.minecraft.world.item.ItemStack

/**
 * Functional interface for creating [ItemStack] instances.
 */
public fun interface ItemStackFactory {
    /**
     * Creates an [ItemStack] instance.
     *
     * @return The stack instance.
     */
    public fun create(): ItemStack
}