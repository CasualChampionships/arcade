package net.casual.arcade.events.entity

import net.casual.arcade.events.level.LevelEvent
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