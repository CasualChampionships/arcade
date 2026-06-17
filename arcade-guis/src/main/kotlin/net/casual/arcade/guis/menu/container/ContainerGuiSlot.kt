/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.menu.container

import net.casual.arcade.guis.core.container.ContainerGui
import net.casual.arcade.guis.menu.slot.EmptyContainer
import net.casual.arcade.guis.menu.slot.GuiSlot
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import java.util.*

public class ContainerGuiSlot(gui: ContainerGui, slot: Int): GuiSlot<ContainerGui>(gui, slot) {
    override fun getItem(): ItemStack {
        return this.gui.getSlotItem(this.containerSlot).display()
    }
}