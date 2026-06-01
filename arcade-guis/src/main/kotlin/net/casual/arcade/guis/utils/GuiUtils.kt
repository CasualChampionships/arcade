/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.utils

import net.casual.arcade.guis.core.Gui
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

private object NotAHashedStack: HashedStack {
    override fun matches(stack: ItemStack, hasher: HashedPatchMap.HashGenerator): Boolean {
        return false
    }
}