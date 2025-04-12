/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.extensions

import net.minecraft.world.entity.Entity

public interface TransferableEntityExtension: Extension {
    public fun transfer(entity: Entity, respawned: Boolean): Extension
}