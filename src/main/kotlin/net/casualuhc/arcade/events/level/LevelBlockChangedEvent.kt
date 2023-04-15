package net.casualuhc.arcade.events.level

import net.casualuhc.arcade.events.core.Event
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.state.BlockState

data class LevelBlockChangedEvent(
    val level: ServerLevel,
    val pos: BlockPos,
    val old: BlockState,
    val new: BlockState
): Event()