/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.impl

import net.casual.arcade.util.mixins.TrackedEntityAccessor
import net.minecraft.server.level.ChunkMap.TrackedEntity
import net.minecraft.server.level.ServerEntity
import net.minecraft.server.network.ServerPlayerConnection
import net.minecraft.world.entity.Entity

/**
 * We wrap the tracked entity into a new class because
 * [TrackedEntity] by default is a package-private class.
 */
@JvmInline
public value class WrappedTrackedEntity(public val tracked: TrackedEntity) {
    /**
     * Gets the [Entity] being tracked.
     *
     * @return The tracked entity.
     */
    public fun getEntity(): Entity {
        return (this.tracked as TrackedEntityAccessor).entity
    }

    /**
     * Gets the [ServerEntity] being tracked.
     *
     * @return The server entity.
     */
    public fun getServerEntity(): ServerEntity {
        return (this.tracked as TrackedEntityAccessor).serverEntity
    }

    /**
     * Gets the player connections which are tracking this entity.
     *
     * @return The player connections.
     */
    public fun getObservers(): Set<ServerPlayerConnection> {
        return (this.tracked as TrackedEntityAccessor).seenBy
    }

    /**
     * Gets the tracking range for this entity.
     *
     * @return The tracking range.
     */
    public fun getRange(): Int {
        return (this.tracked as TrackedEntityAccessor).range
    }
}