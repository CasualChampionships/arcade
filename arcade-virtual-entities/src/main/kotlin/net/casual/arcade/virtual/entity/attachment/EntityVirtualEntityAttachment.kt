/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.attachment

import net.casual.arcade.utils.math.location.LocationWithLevel
import net.casual.arcade.utils.math.location.LocationWithLevel.Companion.locationWithLevel
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity

public class EntityVirtualEntityAttachment(private val entity: Entity): TrackedVirtualEntityAttachment() {
    override val origin: LocationWithLevel<ServerLevel>
        get() = this.entity.locationWithLevel.server()
}