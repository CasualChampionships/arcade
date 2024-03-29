package net.casual.arcade.settings.display

import net.casual.arcade.items.HashableItemStack
import net.casual.arcade.settings.GameSetting
import net.minecraft.world.item.ItemStack

public class DisplayableGameSetting<T: Any>(
    public val display: ItemStack,
    public val setting: GameSetting<T>,
    private val options: Map<HashableItemStack, T>
) {
    public val optionCount: Int
        get() = this.options.size

    public fun forEachOption(consumer: (ItemStack, T) -> Unit) {
        for ((hashable, value) in this.options) {
            consumer(hashable.stack, value)
        }
    }

    public fun getValue(option: ItemStack): T? {
        return this.options[HashableItemStack(option)]
    }
}