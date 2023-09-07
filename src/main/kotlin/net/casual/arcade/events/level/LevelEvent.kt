package net.casual.arcade.events.level

import net.casual.arcade.events.core.Event
import net.casual.arcade.events.player.PlayerEvent
import net.casual.arcade.minigame.Minigame
import net.minecraft.server.level.ServerLevel

/**
 * Superclass for all level-related events.
 *
 * This is to be able to filter out [LevelEvent]'s
 * in [Minigame]s, allowing you to only listen to
 * [LevelEvent]s for the levels in the given [Minigame].
 *
 * @see Minigame.registerMinigameEvent
 */
interface LevelEvent: Event {
    /**
     * The [ServerLevel] that is tied to the event.
     */
    val level: ServerLevel
}