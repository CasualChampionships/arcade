/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.settings.display

import net.casual.arcade.minigame.settings.GameSetting
import net.casual.arcade.utils.ItemUtils.lore
import net.casual.arcade.utils.component.gray
import net.casual.arcade.utils.component.green
import net.casual.arcade.utils.component.italicize
import net.casual.arcade.utils.component.unitalicize
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.StringTag
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.ItemLore
import kotlin.jvm.optionals.getOrNull

public class MenuGameSetting<T: Any>(
    private val display: ItemStack,
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

    public fun display(): ItemStack {
        return this.display.copy()
    }

    public fun displayWithLore(): ItemStack {
        val stack = this.display()

        val lore = ArrayList<Component>()
        lore.addAll(stack.getOrDefault(DataComponents.LORE, ItemLore.EMPTY).lines())
        lore.add(Component.empty())

        val selected = this.selected()
        for (option in this.options) {
            lore.add(this.createOptionLine(option.name, option == selected))
        }
        if (selected == null) {
            val value = Component.literal(this.value().toString()).italicize()
            lore.add(this.createOptionLine(value, true))
        }
        stack.lore(lore)
        return stack
    }

    private fun createOptionLine(name: Component, selected: Boolean): Component {
        if (selected) {
            return Component.literal(SELECTED_PREFIX).append(name).green().unitalicize()
        }
        return Component.literal(UNSELECTED_PREFIX).append(name).gray().unitalicize()
    }

    public data class Option<T: Any>(
        public val id: String,
        public val name: Component,
        public val value: T
    )

    private companion object {
        private const val SELECTED_PREFIX = "▶ "
        private const val UNSELECTED_PREFIX = "   "
    }
}
