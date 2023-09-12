package net.casual.arcade.items

import net.minecraft.world.item.ItemStack

/**
 * This interface provides a way of getting the
 * custom model id for a given [ItemStack].
 *
 * @see ResourcePackItemModeller
 */
public interface ItemModeller {
    /**
     * This gets the custom model id of a given
     * [ItemStack] to send to the client.
     *
     * @param stack The stack to get the id of.
     * @return The custom model id.
     */
    public fun getModelId(stack: ItemStack): Int
}