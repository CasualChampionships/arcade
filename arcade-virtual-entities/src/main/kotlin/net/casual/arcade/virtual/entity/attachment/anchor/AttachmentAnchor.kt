/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.attachment.anchor

import net.casual.arcade.utils.math.location.Location
import net.minecraft.server.level.ServerLevel
import org.jetbrains.annotations.ApiStatus.Experimental

public interface AttachmentAnchor {
    public fun location(): Location

    @Experimental
    public fun level(): ServerLevel? {
        return null
    }
}