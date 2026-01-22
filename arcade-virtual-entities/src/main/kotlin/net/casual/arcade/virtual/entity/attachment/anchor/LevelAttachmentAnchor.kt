/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.attachment.anchor

import net.casual.arcade.utils.math.location.Location
import net.casual.arcade.utils.math.location.LocationWithLevel
import net.minecraft.server.level.ServerLevel

public class LevelAttachmentAnchor(
    public val level: ServerLevel
): AttachmentAnchor {
    override fun location(): LocationWithLevel<ServerLevel> {
        return Location.DEFAULT.with(this.level)
    }
}