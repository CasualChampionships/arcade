/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.attachment.anchor

import net.casual.arcade.utils.math.location.LocationWithLevel
import net.casual.arcade.virtual.entity.ParentVirtualEntity
import net.casual.arcade.virtual.entity.utils.location
import net.minecraft.server.level.ServerLevel

public class ParentAttachmentAnchor(
    public val parent: ParentVirtualEntity
): AttachmentAnchor {
    override fun location(): LocationWithLevel<ServerLevel> {
        return this.parent.location()
    }
}