/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.sgui

import eu.pb4.sgui.api.ClickType
import eu.pb4.sgui.api.gui.SlotBasedGui
import net.casual.arcade.events.server.player.PlayerSlotClickEvent
import net.minecraft.world.item.ItemStack

@Deprecated("Use arcade's Gui library instead")
public val PlayerSlotClickEvent.type: ClickType
    get() = ClickType.toClickType(this.input, this.button, this.index)

@Deprecated("Use arcade's Gui library instead")
public fun SlotBasedGui.setSlot(index: Int, stack: ItemStack, callback: () -> Unit) {
    this.setSlot(index, stack) { _, _, _, _ -> callback.invoke() }
}

@Deprecated("Use arcade's Gui library instead")
public fun SlotBasedGui.setSlot(index: Int, stack: ItemStack, callback: (ClickType) -> Unit) {
    this.setSlot(index, stack) { _, type, _, _ -> callback.invoke(type) }
}

@Deprecated("Use arcade's Gui library instead")
public fun SlotBasedGui.setSlotGrid(
    origin: Int,
    width: Int,
    height: Int,
    stack: ItemStack,
    callback: (ClickType) -> Unit
) {
    this.setSlotGrid(origin, width, height, { _, _ -> stack }, callback)
}

@Deprecated("Use arcade's Gui library instead")
public fun SlotBasedGui.setSlotGrid(
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

@Deprecated("Use arcade's Gui library instead")
public fun SlotBasedGui.clearSlotGrid(origin: Int, width: Int, height: Int) {
    for (i in 0..< width) {
        for (j in 0..< height) {
            this.clearSlot(origin + j * 9 + i)
        }
    }
}
