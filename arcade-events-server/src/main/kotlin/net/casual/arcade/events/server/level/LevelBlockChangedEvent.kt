package net.casual.arcade.events.server.level

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.state.BlockState

public data class LevelBlockChangedEvent(
    override val level: ServerLevel,
    override val pos: BlockPos,
    val old: BlockState,
    val new: BlockState
): LocatedLevelEvent