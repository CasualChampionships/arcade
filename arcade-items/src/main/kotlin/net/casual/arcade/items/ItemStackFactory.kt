package net.casual.arcade.items

import net.minecraft.core.component.DataComponents
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import kotlin.reflect.KProperty

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

    /**
     * Allows delegation of this factory.
     */
    public operator fun getValue(any: Any, property: KProperty<*>): ItemStack {
        return this.create()
    }

    public companion object {
        public fun modelled(item: Item, model: ResourceLocation): ItemStackFactory {
            return ItemStackFactory {
                val stack = ItemStack(item)
                stack.set(DataComponents.ITEM_MODEL, model)
                stack
            }
        }

        public fun modelled(item: Item, model: ResourceLocation, modifier: (ItemStack) -> Unit): ItemStackFactory {
            return ItemStackFactory {
                val stack = ItemStack(item)
                modifier.invoke(stack)
                stack.set(DataComponents.ITEM_MODEL, model)
                stack
            }
        }
    }
}