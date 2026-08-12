/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.pathfinding.movement.types

import net.casual.arcade.npc.pathfinding.PathNode
import net.casual.arcade.npc.pathfinding.PathfindingContext
import net.casual.arcade.npc.pathfinding.execution.ClimbExecutor
import net.casual.arcade.npc.pathfinding.execution.MovementExecutor
import net.casual.arcade.npc.pathfinding.execution.WalkExecutor
import net.casual.arcade.npc.pathfinding.movement.*
import net.casual.arcade.utils.arcade
import net.minecraft.core.Direction
import net.minecraft.resources.Identifier
import net.minecraft.util.Mth
import net.minecraft.world.level.pathfinder.PathType

public object ClimbMovementType: MovementType {
    override val id: Identifier = arcade("climb")

    public const val UP: Int = 0
    public const val DOWN: Int = 1
    public const val MOUNT: Int = 2

    override val minimumCostPerBlock: Double
        get() = MovementCosts.WALK_ONE_BLOCK

    override fun candidates(context: PathfindingContext, from: PathNode, out: MovementCandidates) {
        if (!context.settings.canClimb) {
            return
        }

        val feet = Mth.floor(from.surface)
        if (context.isClimbable(from.x, feet, from.z)) {
            this.verticalCandidates(context, from, feet, out)
        }
        this.mountCandidates(context, from, feet, out)
    }

    override fun create(context: PathfindingContext, from: PathNode, to: PathNode, data: Int, cost: Double): Movement {
        return SimpleMovement(
            this, from, to, cost, sprintable = false, pathType = PathType.WALKABLE, factory = { movement -> this.createExecutor(movement, data) }
        )
    }

    private fun verticalCandidates(context: PathfindingContext, from: PathNode, feet: Int, out: MovementCandidates) {
        val above = feet + 1
        if (context.isClimbable(from.x, above, from.z) && this.canHold(context, from.x, from.z, above.toDouble())) {
            out.acceptSurface(from.x, from.z, above.toDouble(), MovementCosts.CLIMB_UP_ONE_BLOCK, UP)
        }

        val below = feet - 1
        if (context.isClimbable(from.x, below, from.z) && this.canHold(context, from.x, from.z, below.toDouble())) {
            out.acceptSurface(from.x, from.z, below.toDouble(), MovementCosts.CLIMB_DOWN_ONE_BLOCK, DOWN)
        }
    }

    private fun mountCandidates(context: PathfindingContext, from: PathNode, feet: Int, out: MovementCandidates) {
        for (direction in Direction.Plane.HORIZONTAL) {
            val x = from.x + direction.stepX
            val z = from.z + direction.stepZ
            if (!context.isClimbable(x, feet, z)) {
                continue
            }
            if (context.findSupport(x, z, from.surface, from.surface) != PathfindingContext.NO_BLOCK) {
                continue
            }
            if (!this.canHold(context, x, z, from.surface)) {
                continue
            }
            if (!MovementChecks.isCorridorClear(context, from.x, from.z, from.surface, x, z, from.surface)) {
                continue
            }

            out.acceptSurface(x, z, from.surface, MovementCosts.WALK_ONE_BLOCK, MOUNT)
        }
    }

    private fun canHold(context: PathfindingContext, x: Int, z: Int, surface: Double): Boolean {
        return MovementChecks.isStandable(context, x, z, surface) && MovementChecks.isBodyClear(context, x, z, surface)
    }

    private fun createExecutor(movement: Movement, data: Int): MovementExecutor {
        if (data == MOUNT) {
            return WalkExecutor(movement)
        }
        return ClimbExecutor(movement, descending = data == DOWN)
    }
}
