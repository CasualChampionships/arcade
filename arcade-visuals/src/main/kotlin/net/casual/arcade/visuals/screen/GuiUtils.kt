/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.screen

import eu.pb4.sgui.api.ClickType
import eu.pb4.sgui.api.gui.SlotGuiInterface
import net.casual.arcade.events.server.player.PlayerSlotClickEvent
import net.minecraft.world.item.ItemStack

public val PlayerSlotClickEvent.type: ClickType
    get() = ClickType.toClickType(this.action, this.button, this.index)

public fun SlotGuiInterface.setSlot(index: Int, stack: ItemStack, callback: () -> Unit) {
    this.setSlot(index, stack) { _, _, _, _ -> callback.invoke() }
}

public fun SlotGuiInterface.setSlot(index: Int, stack: ItemStack, callback: (ClickType) -> Unit) {
    this.setSlot(index, stack) { _, type, _, _ -> callback.invoke(type) }
}

public fun SlotGuiInterface.setSlotGrid(
    origin: Int,
    width: Int,
    height: Int,
    stack: ItemStack,
    callback: (ClickType) -> Unit
) {
    this.setSlotGrid(origin, width, height, { _, _ -> stack }, callback)
}

public fun SlotGuiInterface.setSlotGrid(
    origin: Int,
    width: Int,
    height: Int,
    stack: (x: Int, y: Int) -> ItemStack,
    callback: (ClickType) -> Unit
) {
    for (i in 0..< width) {
        for (j in 0..< height) {
            val index = origin + j * 9 + i
            this.setSlot(index, stack.invoke(i, j), callback)
        }
    }
}
