/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.menu

import net.casual.arcade.guis.core.ContainerGui
import net.casual.arcade.guis.utils.invalidateRemoteSlots
import net.minecraft.network.chat.Component
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.ItemStack

public open class ContainerGuiMenu(
    gui: ContainerGui,
    containerId: Int,
    inventory: Inventory
): GuiMenu<ContainerGui>(gui, containerId) {
    init {
        this.initialize(inventory)
    }

    protected open fun initialize(inventory: Inventory) {
        val size = this.gui.getContainerSize()
        for (i in 0..<size) {
            this.addSlot(GuiSlot(this.gui, i, 0, 0))
        }

        if (this.gui.isInventoryOverridden()) {
            for (i in 0..<Inventory.INVENTORY_SIZE) {
                this.addSlot(GuiSlot(this.gui, size + i, 0, 0))
            }
        } else {
            this.addStandardInventorySlots(inventory, 0, 0)
        }
    }

    override fun quickMoveStack(player: Player, slotIndex: Int): ItemStack {
        return ItemStack.EMPTY
    }

    override fun removed(player: Player) {
        if (this.gui.isInventoryOverridden()) {
            player.inventoryMenu.invalidateRemoteSlots()
        }
    }

    public class Provider(public val gui: ContainerGui): MenuProvider {
        override fun getDisplayName(): Component {
            return this.gui.getTitle()
        }

        override fun createMenu(containerId: Int, inventory: Inventory, player: Player): AbstractContainerMenu {
            return ContainerGuiMenu(this.gui, containerId, inventory)
        }
    }
}