package net.casual.arcade.events.level

import net.minecraft.core.BlockPos

public interface LocatedLevelEvent: LevelEvent {
    public val pos: BlockPos
}