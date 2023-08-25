package net.casual.arcade.events.level

import net.minecraft.server.level.ServerLevel

data class LevelTickEvent(
    override val level: ServerLevel
): LevelEvent