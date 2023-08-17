package net.casualuhc.arcade.events.minigame

import net.casualuhc.arcade.events.core.Event
import net.casualuhc.arcade.minigame.Minigame

interface MinigameEvent: Event {
    val minigame: Minigame
}