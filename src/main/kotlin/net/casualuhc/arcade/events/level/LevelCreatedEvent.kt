package net.casualuhc.arcade.events.level

import net.casualuhc.arcade.events.core.Event
import net.minecraft.server.level.ServerLevel

class LevelCreatedEvent(
    val level: ServerLevel
): Event()