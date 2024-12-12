package net.casual.arcade.events.server.player

import net.casual.arcade.events.common.Event
import net.minecraft.server.level.ServerPlayer

/**
 * Superclass for all player-related events.
 */
public interface PlayerEvent: Event {
    /**
     * The [ServerPlayer] that is tied to the event.
     */
    public val player: ServerPlayer
}