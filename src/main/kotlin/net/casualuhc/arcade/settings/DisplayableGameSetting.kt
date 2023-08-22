package net.casualuhc.arcade.settings

import net.minecraft.world.item.ItemStack

class DisplayableGameSetting<T: Any>(
    val display: ItemStack,
    val setting: GameSetting<T>,
    val options: Map<ItemStack, T>
)