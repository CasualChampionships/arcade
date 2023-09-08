package net.casual.arcade.settings

import net.casual.arcade.items.HashableItemStack
import net.minecraft.world.item.ItemStack

class DisplayableGameSetting<T: Any>(
    val display: ItemStack,
    val setting: GameSetting<T>,
    private val options: Map<HashableItemStack, T>
) {
    fun getOptions(): List<Pair<ItemStack, T>> {
        val options = ArrayList<Pair<ItemStack, T>>()
        for ((hashable, value) in this.options) {
            options.add(hashable.stack to value)
        }
        return options
    }

    fun getValue(option: ItemStack): T? {
        return this.options[HashableItemStack(option)]
    }
}