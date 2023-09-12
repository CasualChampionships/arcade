package net.casual.arcade.events.level

import net.minecraft.server.level.ServerLevel

public data class LevelCreatedEvent(
    override val level: ServerLevel
): LevelEvent