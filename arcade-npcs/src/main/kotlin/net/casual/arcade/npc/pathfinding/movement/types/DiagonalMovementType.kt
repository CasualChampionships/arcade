/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.pathfinding.movement.types

import net.casual.arcade.npc.pathfinding.PathNode
import net.casual.arcade.npc.pathfinding.PathfindingContext
import net.casual.arcade.npc.pathfinding.execution.MovementExecutor
import net.casual.arcade.npc.pathfinding.execution.WalkExecutor
import net.casual.arcade.npc.pathfinding.movement.*
import net.casual.arcade.utils.arcade
import net.minecraft.core.Direction
import net.minecraft.resources.Identifier
import net.minecraft.world.level.pathfinder.PathType

public object DiagonalMovementType: MovementType {
    override val id: Identifier = arcade("diagonal")

    public const val DISTANCE: Double = 1.4142135623730951

    override fun candidates(context: PathfindingContext, from: PathNode, out: MovementCandidates) {
        for (direction in Direction.Plane.HORIZONTAL) {
            val other = direction.clockWise
            val x = from.x + direction.stepX + other.stepX
            val z = from.z + direction.stepZ + other.stepZ

            val support = context.findSupport(
                x, z, from.surface + context.stepHeight, from.surface - context.stepHeight
            )
            if (support == PathfindingContext.NO_BLOCK) {
                continue
            }

            val surface = context.getSurface(x, support, z)
            if (surface - from.surface > context.stepHeight) {
                continue
            }
            if (!MovementChecks.isStandable(context, x, z, surface)) {
                continue
            }
            if (!MovementChecks.canCutCorner(context, from.x, from.z, from.surface, direction, other)) {
                continue
            }
            if (!MovementChecks.isCorridorClear(context, from.x, from.z, from.surface, x, z, surface)) {
                continue
            }

            val cost = MovementCosts.travel(context, x, support, z, surface, DISTANCE, context.settings.canSprint)
            out.acceptSurface(x, z, surface, cost)
        }
    }

    override fun create(context: PathfindingContext, from: PathNode, to: PathNode, data: Int, cost: Double): Movement {
        return SimpleMovement(
            this, from, to, cost, sprintable = true, pathType = PathType.WALKABLE, factory = ::WalkExecutor
        )
    }
}
