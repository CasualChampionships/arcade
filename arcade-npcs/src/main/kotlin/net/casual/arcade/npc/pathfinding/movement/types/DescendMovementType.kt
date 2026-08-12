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

public object DescendMovementType: MovementType {
    override val id: Identifier = arcade("descend")

    override fun candidates(context: PathfindingContext, from: PathNode, out: MovementCandidates) {
        val lowest = from.surface - (1.0 + context.stepHeight)
        for (direction in Direction.Plane.HORIZONTAL) {
            val x = from.x + direction.stepX
            val z = from.z + direction.stepZ
            val support = context.findSupport(x, z, from.surface - context.stepHeight, lowest)
            if (support == PathfindingContext.NO_BLOCK) {
                continue
            }

            val surface = context.getSurface(x, support, z)
            val drop = from.surface - surface
            if (drop <= context.stepHeight || drop > 1.0 + context.stepHeight) {
                continue
            }
            if (!MovementChecks.isStandable(context, x, z, surface)) {
                continue
            }
            if (!MovementChecks.isColumnClear(context, x, z, surface, from.surface)) {
                continue
            }

            val cost = MovementCosts.travel(
                context, x, support, z, surface, 1.0, context.settings.canSprint
            )
            out.acceptSurface(x, z, surface, cost)
        }
    }

    override fun create(context: PathfindingContext, from: PathNode, to: PathNode, data: Int, cost: Double): Movement {
        return SimpleMovement(
            this, from, to, cost, sprintable = true, pathType = PathType.WALKABLE, factory = ::WalkExecutor
        )
    }
}
