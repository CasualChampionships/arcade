/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.menu

import net.casual.arcade.guis.core.Gui
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack

public abstract class GuiMenu<G: Gui>(
    public val gui: G,
    containerId: Int
): AbstractContainerMenu(gui.getMenuType(), containerId) {
    public fun tick() {
        this.gui.tick()
    }

    override fun stillValid(player: Player): Boolean {
        return this.gui.valid()
    }

    override fun canTakeItemForPickAll(carried: ItemStack, target: Slot): Boolean {
        return target !is GuiSlot && super.canTakeItemForPickAll(carried, target)
    }
}