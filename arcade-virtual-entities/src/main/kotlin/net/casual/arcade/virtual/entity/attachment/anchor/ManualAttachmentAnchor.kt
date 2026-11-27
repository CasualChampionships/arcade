/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.attachment.anchor

import net.casual.arcade.utils.math.location.Location
import net.casual.arcade.utils.math.location.LocationWithLevel
import net.casual.arcade.virtual.entity.location.VirtualHeadRotation
import net.minecraft.server.level.ServerLevel

public class ManualAttachmentAnchor(
    public var location: Location,
    public var headRotation: Float,
    public val level: ServerLevel
): AttachmentAnchor {
    public constructor(
        location: LocationWithLevel<ServerLevel>,
        headRotation: Float = location.yRot,
    ): this(location.location, headRotation, location.level)

    override fun location(): Location {
        return this.location
    }

    override fun headRotation(): Float {
        return this.headRotation
    }

    override fun level(): ServerLevel {
        return this.level
    }
}