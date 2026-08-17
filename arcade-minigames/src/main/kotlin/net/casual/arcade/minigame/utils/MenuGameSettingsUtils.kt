/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.utils

import net.casual.arcade.minigame.settings.display.MenuGameSettingBuilder
import net.casual.arcade.utils.ItemUtils
import net.casual.arcade.utils.ItemUtils.named
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack

public fun MenuGameSettingBuilder<Boolean>.defaultOptions() {
    option("enabled", Component.literal("On"), true)
    option("disabled", Component.literal("Off"), false)
}

@Suppress("UnusedReceiverParameter")
public fun MenuGameSettingBuilder<*>.item(item: Item, name: Component): ItemStack {
    return ItemUtils.modelled(item).named(name)
}