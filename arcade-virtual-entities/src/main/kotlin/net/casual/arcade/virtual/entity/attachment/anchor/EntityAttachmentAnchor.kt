/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.attachment.anchor

import net.casual.arcade.utils.math.location.Location
import net.casual.arcade.utils.math.location.Location.Companion.location
import net.casual.arcade.utils.math.location.LocationWithLevel
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity

public class EntityAttachmentAnchor(
    public val entity: Entity
): AttachmentAnchor {
    override fun location(): Location {
        return this.entity.location
    }

    override fun level(): ServerLevel? {
        return this.entity.level() as? ServerLevel
    }
}