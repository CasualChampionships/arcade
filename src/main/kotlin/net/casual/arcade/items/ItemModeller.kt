package net.casual.arcade.items

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import net.minecraft.world.item.Item
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

    public companion object {
        private val CUSTOM_MODEL_DATA = Object2IntOpenHashMap<Item>()

        init {
            CUSTOM_MODEL_DATA.defaultReturnValue(-1)
        }

        public fun getNextIdFor(item: Item): Int {
            val next = CUSTOM_MODEL_DATA.getInt(item) + 1
            CUSTOM_MODEL_DATA.put(item, next)
            return next
        }

        public fun hasIdFor(item: Item, id: Int): Boolean {
            val max = CUSTOM_MODEL_DATA.getInt(item)
            return id in 0..max
        }
    }
}