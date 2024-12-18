/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.level

import net.casual.arcade.events.common.Event
import net.minecraft.server.level.ServerLevel

/**
 * Superclass for all level-related events.
 */
public interface LevelEvent: Event {
    /**
     * The [ServerLevel] that is tied to the event.
     */
    public val level: ServerLevel
}