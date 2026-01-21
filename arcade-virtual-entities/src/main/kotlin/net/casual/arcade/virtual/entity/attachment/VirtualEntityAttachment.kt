/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.attachment

import net.casual.arcade.utils.math.location.LocationWithLevel
import net.casual.arcade.virtual.entity.VirtualEntity
import net.minecraft.server.level.ServerLevel

public interface VirtualEntityAttachment {
    public val origin: LocationWithLevel<ServerLevel>

    public fun attach(entity: VirtualEntity): Boolean

    public fun detach(entity: VirtualEntity): Boolean

    public fun attached(): Collection<VirtualEntity>

    public fun tick() {
        for (entity in this.attached()) {
            entity.tick()
        }
    }
}