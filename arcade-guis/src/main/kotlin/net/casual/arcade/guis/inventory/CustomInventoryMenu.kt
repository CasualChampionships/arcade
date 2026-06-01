/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.inventory

import net.casual.arcade.guis.utils.SlotClickAction
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.ContainerInput
import net.minecraft.world.inventory.InventoryMenu

public open class CustomInventoryMenu(
    inventory: CustomInventory,
    active: Boolean,
    owner: ServerPlayer
): InventoryMenu(inventory, active, owner) {
    override fun owner(): ServerPlayer {
        return super.owner() as ServerPlayer
    }

    override fun clicked(slot: Int, button: Int, input: ContainerInput, player: Player) {
        if (!this.click(slot, SlotClickAction.from(input, button, slot))) {
            super.clicked(slot, button, input, player)
        }
    }

    protected open fun click(slot: Int, action: SlotClickAction): Boolean {
        return false
    }
}