package net.casual.arcade.events.server.level

import net.minecraft.server.level.ServerLevel

public data class LevelTickEvent(
    override val level: ServerLevel
): LevelEvent