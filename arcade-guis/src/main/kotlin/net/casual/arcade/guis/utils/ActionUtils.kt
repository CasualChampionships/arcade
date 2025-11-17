/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.utils

import net.casual.arcade.guis.core.SlotInteractAction

public fun SlotInteractAction.isUse(): Boolean {
    return this == SlotInteractAction.Use
        || this is SlotInteractAction.UseOnBlock
        || this is SlotInteractAction.UseOnEntity
}