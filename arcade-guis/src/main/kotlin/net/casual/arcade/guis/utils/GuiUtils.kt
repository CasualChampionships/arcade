/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.utils

import net.casual.arcade.guis.core.Gui
import net.casual.arcade.guis.core.container.ContainerGui
import net.casual.arcade.guis.core.container.GuiItem
import net.casual.arcade.guis.menu.GuiMenu
import net.minecraft.network.HashedPatchMap
import net.minecraft.network.HashedStack
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.ItemStack

public fun ServerPlayer.getOpenGui(): Gui? {
    val menu = this.containerMenu
    return if (menu is GuiMenu<*>) menu.gui else null
}

public fun AbstractContainerMenu.invalidateRemoteSlots() {
    for (i in this.slots.indices) {
        this.setRemoteSlotUnsafe(i, NotAHashedStack)
    }
}

public fun Gui.ensureMatchingPlayer(other: Gui?) {
    require(other == null || this.player == other.player) { "Mismatching gui players!" }
}

public fun ContainerGui.setSlotGrid(
    origin: Int,
    width: Int,
    height: Int,
    item: GuiItem,
    handler: SlotClickHandler? = null
) {
    this.setSlotGrid(origin, width, height, { _, _ -> item }, handler)
}

public inline fun ContainerGui.setSlotGrid(
    origin: Int,
    width: Int,
    height: Int,
    item: (x: Int, y: Int) -> GuiItem,
    handler: SlotClickHandler? = null
) {
    for (i in 0..< width) {
        for (j in 0..< height) {
            val index = origin + j * 9 + i
            this.setSlot(index, item.invoke(i, j), handler)
        }
    }
}

public fun ContainerGui.clearSlotGrid(origin: Int, width: Int, height: Int) {
    for (i in 0..< width) {
        for (j in 0..< height) {
            this.clearSlot(origin + j * 9 + i)
        }
    }
}

private object NotAHashedStack: HashedStack {
    override fun matches(stack: ItemStack, hasher: HashedPatchMap.HashGenerator): Boolean {
        return false
    }
}