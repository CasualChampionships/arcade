package net.casual.arcade.events.level

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.state.BlockState

public data class LevelBlockChangedEvent(
    override val level: ServerLevel,
    val pos: BlockPos,
    val old: BlockState,
    val new: BlockState
): LevelEvent