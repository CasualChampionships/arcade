package net.casual.arcade.events.level

import net.casual.arcade.events.core.Event
import net.minecraft.server.level.ServerLevel

interface LevelEvent: Event {
    val level: ServerLevel
}