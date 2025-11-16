/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.temp

import net.casual.arcade.guis.ducks.ModifiableInventory
import net.casual.arcade.guis.inventory.CustomInventory
import net.minecraft.server.level.ServerPlayer

public fun ServerPlayer.setCustomInventory(inventory: CustomInventory) {
    (this as ModifiableInventory).`arcade$setCustomInventory`(inventory)
}