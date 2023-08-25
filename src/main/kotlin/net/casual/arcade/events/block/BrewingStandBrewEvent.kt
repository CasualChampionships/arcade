package net.casual.arcade.events.block

import net.casual.arcade.events.core.CancellableEvent
import net.casual.arcade.events.level.LevelEvent
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity
import net.minecraft.world.level.block.state.BlockState

data class BrewingStandBrewEvent(
    override val level: ServerLevel,
    val pos: BlockPos,
    val state: BlockState,
    val entity: BrewingStandBlockEntity,
): CancellableEvent.Default(), LevelEvent