/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.interaction

import net.casual.arcade.virtual.entity.SimpleVirtualEntity
import net.casual.arcade.virtual.entity.VirtualEntity
import net.casual.arcade.virtual.entity.attachment.VirtualEntityAttachment
import net.casual.arcade.virtual.entity.tracker.ObserverTracker
import net.casual.arcade.virtual.entity.utils.EntityDataAccessors
import net.minecraft.world.entity.EntityType

public class SimpleVirtualInteractionEntity(
    attachment: VirtualEntityAttachment,
    observers: ObserverTracker
): SimpleVirtualEntity(EntityType.INTERACTION, attachment, observers) {
    public fun setWidth(width: Float) {
        this.setDataEntry(EntityDataAccessors.Interaction.WIDTH, width)
    }

    public fun setHeight(height: Float) {
        this.setDataEntry(EntityDataAccessors.Interaction.HEIGHT, height)
    }

    public fun setResponse(response: Boolean) {
        this.setDataEntry(EntityDataAccessors.Interaction.RESPONSE, response)
    }

    public fun setInteractionHandler(handler: VirtualEntity.InteractionHandler) {
        this.setInteractionHandlerProvider { _ -> handler }
    }
}