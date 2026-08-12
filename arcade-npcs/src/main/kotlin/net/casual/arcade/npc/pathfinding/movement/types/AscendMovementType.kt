/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.pathfinding.movement.types

import net.casual.arcade.npc.pathfinding.PathNode
import net.casual.arcade.npc.pathfinding.PathfindingContext
import net.casual.arcade.npc.pathfinding.execution.JumpExecutor
import net.casual.arcade.npc.pathfinding.execution.MovementExecutor
import net.casual.arcade.npc.pathfinding.movement.*
import net.casual.arcade.utils.arcade
import net.minecraft.core.Direction
import net.minecraft.resources.Identifier
import net.minecraft.world.level.pathfinder.PathType

public object AscendMovementType: MovementType {
    override val id: Identifier = arcade("ascend")

    override fun candidates(context: PathfindingContext, from: PathNode, out: MovementCandidates) {
        val maxJump = context.settings.maxJumpHeight
        for (direction in Direction.Plane.HORIZONTAL) {
            val x = from.x + direction.stepX
            val z = from.z + direction.stepZ
            val support = context.findSupport(
                x, z, from.surface + maxJump, from.surface + context.stepHeight
            )
            if (support == PathfindingContext.NO_BLOCK) {
                continue
            }

            val surface = context.getSurface(x, support, z)
            val rise = surface - from.surface
            if (rise <= context.stepHeight || rise > maxJump) {
                continue
            }
            if (MovementChecks.canStepOnto(context, from.surface, x, support, z, direction)) {
                continue
            }
            if (!MovementChecks.isStandable(context, x, z, surface)) {
                continue
            }
            if (!MovementChecks.isColumnClear(context, from.x, from.z, from.surface, from.surface + rise)) {
                continue
            }
            if (!MovementChecks.isCorridorClear(context, from.x, from.z, from.surface, x, z, surface)) {
                continue
            }

            val cost = MovementCosts.travel(
                context, x, support, z, surface, 1.0, context.settings.canSprint
            ) + MovementCosts.JUMP_ONE_BLOCK
            out.acceptSurface(x, z, surface, cost)
        }
    }

    override fun create(context: PathfindingContext, from: PathNode, to: PathNode, data: Int, cost: Double): Movement {
        return SimpleMovement(
            this, from, to, cost, sprintable = true, pathType = PathType.WALKABLE, factory = ::JumpExecutor
        )
    }
}
