/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.entity

import net.casual.arcade.events.server.level.LevelEvent
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity

public interface EntityEvent: LevelEvent {
    /**
     * The [Entity] that is tied to the event.
     */
    public val entity: Entity

    /**
     * The [entity]'s [ServerLevel].
     * This may be overridden for events where
     * the level may not be the same as the [entity]'s.
     */
    override val level: ServerLevel
        get() = this.entity.level() as ServerLevel
}