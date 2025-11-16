/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.inventory

import net.casual.arcade.utils.entity.EntityTransferReason
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.EntityEquipment
import net.minecraft.world.entity.player.Inventory

public open class CustomInventory(
    player: ServerPlayer,
    equipment: EntityEquipment
): Inventory(player, equipment) {
    public fun player(): ServerPlayer {
        return this.player as ServerPlayer
    }

    public open fun menu(): CustomInventoryMenu {
        return CustomInventoryMenu(this, true, this.player())
    }

    public open fun transfer(player: ServerPlayer, reason: EntityTransferReason): CustomInventory? {
        return null
    }
}