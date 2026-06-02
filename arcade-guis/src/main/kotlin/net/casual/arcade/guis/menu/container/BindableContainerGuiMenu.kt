/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.menu.container

import net.casual.arcade.guis.core.container.BindableContainerGui
import net.casual.arcade.guis.menu.GuiMenu
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu

public class BindableContainerGuiMenu(
    gui: BindableContainerGui,
    containerId: Int,
    inventory: Inventory
): ContainerGuiMenu<BindableContainerGui>(gui, containerId, inventory) {
    override fun updateSlots() {
        for (slot in this.gui.checkDirtySlots()) {
            this.slots[slot] = this.gui.getBoundSlot(slot) ?: ContainerGuiSlot(this.gui, slot)
        }
    }

    public class Provider(override val gui: BindableContainerGui): GuiMenu.Provider<BindableContainerGui> {
        override fun getDisplayName(): Component {
            return this.gui.getTitle()
        }

        override fun createMenu(containerId: Int, inventory: Inventory, player: Player): AbstractContainerMenu {
            return BindableContainerGuiMenu(this.gui, containerId, inventory)
        }
    }
}