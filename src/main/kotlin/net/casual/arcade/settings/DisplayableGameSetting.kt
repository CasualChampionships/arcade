package net.casual.arcade.settings

import net.casual.arcade.items.HashableItemStack
import net.minecraft.world.item.ItemStack

public class DisplayableGameSetting<T: Any>(
    public val display: ItemStack,
    public val setting: GameSetting<T>,
    private val options: Map<HashableItemStack, T>
) {
    public fun forEachOption(consumer: (ItemStack, T) -> Unit) {
        for ((hashable, value) in this.options) {
            consumer(hashable.stack, value)
        }
    }

    public fun getValue(option: ItemStack): T? {
        return this.options[HashableItemStack(option)]
    }
}