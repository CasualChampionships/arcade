/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.hotbar

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.InventoryMenu

public open class CustomInventoryMenu(
    inventory: Inventory,
    active: Boolean,
    owner: ServerPlayer
): InventoryMenu(inventory, active, owner) {
    override fun owner(): ServerPlayer {
        return super.owner() as ServerPlayer
    }
}