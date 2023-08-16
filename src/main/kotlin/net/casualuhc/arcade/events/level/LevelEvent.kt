package net.casualuhc.arcade.events.level

import net.casualuhc.arcade.events.core.Event
import net.minecraft.server.level.ServerLevel

interface LevelEvent: Event {
    val level: ServerLevel
}