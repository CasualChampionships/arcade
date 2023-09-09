package net.casual.arcade.events.minigame

import net.casual.arcade.events.core.Event
import net.casual.arcade.events.level.LevelEvent
import net.casual.arcade.minigame.Minigame

/**
 * Superclass for all minigame-related events.
 *
 * This is to be able to filter out [MinigameEvent]'s
 * in [Minigame]s, allowing you to only listen to
 * [MinigameEvent]s for the levels in the given [Minigame].
 *
 * @see Minigame.registerMinigameEvent
 */
interface MinigameEvent: Event {
    /**
     * The [Minigame] that is tied to the event.
     */
    val minigame: Minigame<*>
}