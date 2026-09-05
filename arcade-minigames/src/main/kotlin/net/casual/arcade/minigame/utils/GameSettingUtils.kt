/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.utils

import net.casual.arcade.guis.core.container.ContainerGui
import net.casual.arcade.guis.utils.SlotClickAction
import net.casual.arcade.minigame.settings.GameSetting
import net.casual.arcade.minigame.settings.GameSettingBuilder
import net.casual.arcade.utils.ItemUtils
import net.casual.arcade.utils.ItemUtils.lore
import net.casual.arcade.utils.ItemUtils.named
import net.casual.arcade.utils.component.gray
import net.casual.arcade.utils.component.green
import net.casual.arcade.utils.component.italicize
import net.casual.arcade.utils.component.unitalicize
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.ItemLore
import kotlin.enums.enumEntries

public fun GameSettingBuilder<Boolean>.defaultOptions() {
    option("enabled", Component.literal("On"), true)
    option("disabled", Component.literal("Off"), false)
}

public inline fun <reified E: Enum<E>> GameSettingBuilder<E>.defaultOptions(
    id: (E) -> String = { value -> value.name },
    name: (E) -> Component = { value -> Component.literal(value.name) }
) {
    for (value in enumEntries<E>()) {
        option(id.invoke(value), name.invoke(value), value)
    }
}


@Suppress("UnusedReceiverParameter")
public fun GameSettingBuilder<*>.item(item: Item, name: Component): ItemStack {
    return ItemUtils.modelled(item).named(name)
}

public fun ContainerGui.addSettingDisplay(slot: Int, setting: GameSetting<*>, interactable: () -> Boolean = { true }) {
    this.setSlot(slot, displayWithLore(setting)) { action ->
        if (interactable.invoke() && action.isMouse && action != SlotClickAction.MouseDoubleClick) {
            setting.cycle(if (action.isRight) -1 else 1)
            this.setSlotItem(slot, displayWithLore(setting))
        }
    }
}

private fun displayWithLore(setting: GameSetting<*>): ItemStack {
    val stack = setting.display() ?: return ItemStack.EMPTY

    val lore = ArrayList<Component>()
    lore.addAll(stack.getOrDefault(DataComponents.LORE, ItemLore.EMPTY).lines())
    lore.add(Component.empty())

    val selected = setting.selected()
    for (option in setting.options) {
        lore.add(createOptionLine(option.name, option == selected))
    }
    if (selected == null) {
        val value = Component.literal(setting.get().toString()).italicize()
        lore.add(createOptionLine(value, true))
    }
    stack.lore(lore)
    return stack
}

private fun createOptionLine(name: Component, selected: Boolean): Component {
    if (selected) {
        return Component.literal("▶ ").append(name).green().unitalicize()
    }
    return Component.literal("   ").append(name).gray().unitalicize()
}