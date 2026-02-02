/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.attachment.anchor

import net.casual.arcade.utils.math.location.Location
import net.casual.arcade.utils.math.location.LocationWithLevel
import net.minecraft.server.level.ServerLevel

public class ManualAttachmentAnchor(
    public var location: Location,
    public val level: ServerLevel
): AttachmentAnchor {
    public constructor(location: LocationWithLevel<ServerLevel>): this(location.location, location.level)

    override fun location(): Location {
        return this.location
    }

    override fun level(): ServerLevel {
        return this.level
    }
}