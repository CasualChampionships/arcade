/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.inventory

import net.casual.arcade.guis.core.SlotClickAction
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack

public open class VirtualInventoryMenu(
    private val inventory: VirtualInventory,
    owner: ServerPlayer
): CustomInventoryMenu(inventory, false, owner) {
    public open fun inventory(): VirtualInventory {
        return this.inventory
    }

    override fun click(slot: Int, action: SlotClickAction): Boolean {
        this.inventory().click(slot, action)
        // TODO: Optimize this
        this.sendAllDataToRemote()
        return true
    }

    override fun quickMoveStack(player: Player, index: Int): ItemStack {
        return ItemStack.EMPTY
    }
}