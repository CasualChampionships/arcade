/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.extensions

import net.casual.arcade.utils.entity.EntityTransferReason
import net.casual.arcade.utils.impl.DelayedActions
import net.minecraft.world.entity.Entity
import org.jetbrains.annotations.ApiStatus.OverrideOnly

/**
 * An extension for [Entity]s which allow transferring
 * between different instances of the same entity.
 *
 * This can happen as a result of an entity travelling
 * between dimensions, respawning, or converting to
 * a different entity.
 *
 * @see EntityExtension
 * @see PlayerExtension
 */
public interface TransferableEntityExtension: Extension {
    /**
     * This method is called when this extension is being
     * transferred to another [Entity] instance, which is
     * provided as the [entity] parameter.
     *
     * @param entity The entity this extension is being
     * transferred to.
     * @param reason The reason for the transfer.
     * @param delayed Actions to be run *after* the entity
     * that is being transferred to is fully initialized.
     * @return The transferred extension.
     */
    @OverrideOnly
    public fun transfer(entity: Entity, reason: EntityTransferReason, delayed: DelayedActions): Extension
}