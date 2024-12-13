package net.casual.arcade.events.server.level

import net.minecraft.core.BlockPos

public interface LocatedLevelEvent: LevelEvent {
    public val pos: BlockPos
}