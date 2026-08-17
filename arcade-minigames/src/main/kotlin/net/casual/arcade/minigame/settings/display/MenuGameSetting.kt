/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.settings.display

import net.casual.arcade.minigame.settings.GameSetting
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.StringTag
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import kotlin.jvm.optionals.getOrNull

public class MenuGameSetting<T: Any>(
    public val display: ItemStack,
    public val setting: GameSetting<T>,
    public val options: List<Option<T>>
) {
    public val name: String
        get() = this.setting.name

    public fun value(): T {
        return this.setting.get()
    }

    public fun selected(): Option<T>? {
        val current = this.setting.get()
        return this.options.firstOrNull { it.value == current }
    }

    public fun cycle(offset: Int) {
        if (this.options.isEmpty()) {
            return
        }
        val current = this.setting.get()
        val index = this.options.indexOfFirst { it.value == current }
        val next = if (index == -1) 0 else (index + offset).mod(this.options.size)
        this.setting.set(this.options[next].value)
    }

    public data class Option<T: Any>(
        public val id: String,
        public val name: Component,
        public val value: T
    )
}
