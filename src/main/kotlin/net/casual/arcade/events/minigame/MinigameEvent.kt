package net.casual.arcade.events.minigame

import net.casual.arcade.events.core.Event
import net.casual.arcade.minigame.Minigame

interface MinigameEvent: Event {
    val minigame: Minigame
}