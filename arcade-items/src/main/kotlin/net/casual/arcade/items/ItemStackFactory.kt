package net.casual.arcade.items

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
}