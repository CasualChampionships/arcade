/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.extensions

import net.casual.arcade.utils.entity.EntityTransferReason
import net.casual.arcade.utils.impl.DelayedInvokers
import net.minecraft.world.entity.Entity
import org.jetbrains.annotations.ApiStatus.OverrideOnly

public interface TransferableEntityExtension: Extension {
    @OverrideOnly
    public fun transfer(entity: Entity, reason: EntityTransferReason, delayed: DelayedInvokers): Extension
}