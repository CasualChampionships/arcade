package net.casual.arcade.events.player

import net.casual.arcade.events.level.LevelEvent
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer

/**
 * Superclass for all player-related events.
 */
public interface PlayerEvent: LevelEvent {
    /**
     * The [ServerPlayer] that is tied to the event.
     */
    public val player: ServerPlayer

    /**
     * The [player]'s [ServerLevel].
     * This may be overridden for events where
     * the level may not be the same as the [player]'s.
     */
    override val level: ServerLevel
        get() = this.player.level() as ServerLevel
}