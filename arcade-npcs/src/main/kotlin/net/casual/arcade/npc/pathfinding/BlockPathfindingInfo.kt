/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.pathfinding

import net.casual.arcade.utils.registries.isOf
import net.minecraft.tags.BlockTags
import net.minecraft.tags.FluidTags
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.FluidState
import net.minecraft.world.level.pathfinder.PathType
import net.minecraft.world.phys.shapes.VoxelShape

/**
 * A cached view of a single block, as seen by a pathfinding search.
 *
 * @param state The block's state.
 * @param fluid The block's fluid state.
 * @param shape The block's collision shape, in block-local coordinates.
 * @param type The block's [PathType], used for hazard and door classification.
 */
public class BlockPathfindingInfo(
    public val state: BlockState,
    public val fluid: FluidState,
    public val shape: VoxelShape,
    public val type: PathType,
    public val isFullCube: Boolean
) {
    public val isCollisionEmpty: Boolean = this.shape.isEmpty

    public val isDamaging: Boolean = when (this.type) {
        PathType.DAMAGING, PathType.LAVA, PathType.FIRE, PathType.DAMAGE_CAUTIOUS -> true
        else -> false
    }

    public val isWater: Boolean = this.fluid.isOf(FluidTags.WATER)

    public val isClimbable: Boolean = this.state.isOf(BlockTags.CLIMBABLE)
}
