package net.casual.arcade.settings

import net.casual.arcade.items.HashableItemStack
import net.minecraft.world.item.ItemStack

class DisplayableGameSetting<T: Any>(
    val display: ItemStack,
    val setting: GameSetting<T>,
    private val options: Map<HashableItemStack, T>
) {
    fun forEachOption(consumer: (ItemStack, T) -> Unit) {
        for ((hashable, value) in this.options) {
            consumer(hashable.stack, value)
        }
    }

    fun getValue(option: ItemStack): T? {
        return this.options[HashableItemStack(option)]
    }
}