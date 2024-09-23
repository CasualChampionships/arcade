package net.casual.arcade.events.level

import net.casual.arcade.events.core.Event
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