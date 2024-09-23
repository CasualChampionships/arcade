package net.casual.arcade.utils

import net.minecraft.world.Container
import net.minecraft.world.item.ItemStack

public fun Container.getAllItems(): List<ItemStack> {
    val items = ArrayList<ItemStack>(this.containerSize)
    for (i in 0 until this.containerSize) {
        items.add(this.getItem(i))
    }
    return items
}