/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.ducks;

import net.casual.arcade.guis.inventory.CustomInventory;

public interface ModifiableInventory {
    void arcade$setCustomInventory(CustomInventory inventory);
    void arcade$removeCustomInventory();
}
