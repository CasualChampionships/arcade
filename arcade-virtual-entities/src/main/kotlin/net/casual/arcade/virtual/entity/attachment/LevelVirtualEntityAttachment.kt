/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.attachment

import net.casual.arcade.utils.math.location.Location
import net.casual.arcade.utils.math.location.LocationWithLevel
import net.minecraft.server.level.ServerLevel

public class LevelVirtualEntityAttachment(level: ServerLevel): TrackedVirtualEntityAttachment() {
    override val origin: LocationWithLevel<ServerLevel> = Location.DEFAULT.with(level)

    override fun tick() {
        this.updateObservers(this.origin.level.players().toSet())

        super.tick()
    }
}