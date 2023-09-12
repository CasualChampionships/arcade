package net.casual.arcade.events.player

import net.casual.arcade.events.level.LevelEvent
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.MinigameEventHandler
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer

/**
 * Superclass for all player-related events.
 *
 * This is to be able to filter out [PlayerEvent]'s
 * in [Minigame]s, allowing you to only listen to
 * [PlayerEvent]s for players in the given [Minigame].
 *
 * This interface also extends [LevelEvent] which
 * allows further filtering.
 *
 * @see MinigameEventHandler.register
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