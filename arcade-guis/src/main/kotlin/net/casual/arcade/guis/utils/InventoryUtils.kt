/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.utils

import net.casual.arcade.guis.ducks.ModifiableInventory
import net.casual.arcade.guis.inventory.CustomInventory
import net.minecraft.server.level.ServerPlayer

public fun ServerPlayer.setCustomInventory(inventory: CustomInventory, keepSelected: Boolean = true) {
    if (keepSelected) {
        inventory.selectedSlot = this.inventory.selectedSlot
    }
    (this as ModifiableInventory).`arcade$setCustomInventory`(inventory)
}

public fun ServerPlayer.removeCustomInventory() {
    (this as ModifiableInventory).`arcade$removeCustomInventory`()
}