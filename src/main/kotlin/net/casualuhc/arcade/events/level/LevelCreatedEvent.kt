package net.casualuhc.arcade.events.level

import net.minecraft.server.level.ServerLevel

data class LevelCreatedEvent(
    override val level: ServerLevel
): LevelEvent