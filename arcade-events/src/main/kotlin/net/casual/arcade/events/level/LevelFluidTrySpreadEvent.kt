package net.casual.arcade.events.level

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.Fluid
import net.minecraft.world.level.material.FluidState

public class LevelFluidTrySpreadEvent(
    override val level: ServerLevel,
    public val liquidPos: BlockPos,
    public val liquidBlockState: BlockState,
    public val direction: Direction,
    public val spreadPos: BlockPos,
    public val spreadBlockState: BlockState,
    public val spreadFluidState: FluidState,
    public val fluid: Fluid,
    public var canSpread: Boolean
): LevelEvent