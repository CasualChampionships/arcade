/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.pathfinding.movement.types

import net.casual.arcade.npc.pathfinding.JumpPhysicsSimulation
import net.casual.arcade.npc.pathfinding.PathNode
import net.casual.arcade.npc.pathfinding.PathfindingContext
import net.casual.arcade.npc.pathfinding.execution.MovementExecutor
import net.casual.arcade.npc.pathfinding.execution.ParkourExecutor
import net.casual.arcade.npc.pathfinding.movement.*
import net.casual.arcade.utils.arcade
import net.minecraft.core.Direction
import net.minecraft.resources.Identifier
import net.minecraft.world.level.pathfinder.PathType

public object ParkourMovementType: MovementType {
    private const val NO_ROOM = -1
    private const val MIN_GAP_DEPTH = 1.0

    public const val WALKING: Int = 0
    public const val SPRINTING: Int = 1

    public const val MIN_DISTANCE: Int = 2
    public const val RISK_PENALTY: Double = 2.0

    override val id: Identifier = arcade("parkour")

    override val minimumCostPerBlock: Double
        get() = MovementCosts.SPRINT_JUMP_ONE_BLOCK

    override fun candidates(context: PathfindingContext, from: PathNode, out: MovementCandidates) {
        val settings = context.settings
        if (!settings.canParkour) {
            return
        }
        if (!context.getBlock(from.x, from.y, from.z).isFullCube) {
            return
        }
        if (context.isSubmerged(from.x, from.z, from.surface)) {
            return
        }

        val physics = context.jumpSimulation
        val overhead = this.roomOver(context, from.x, from.z, from.surface, physics.roomSteps)
        if (overhead == NO_ROOM) {
            return
        }

        val reachable = physics.furthest(settings.canSprint, overhead, -physics.depth)
        val furthest = minOf(settings.maxParkourDistance, reachable)
        if (furthest < MIN_DISTANCE) {
            return
        }

        for (direction in Direction.Plane.HORIZONTAL) {
            this.candidatesTowards(context, from, direction, furthest, overhead, out)
        }
    }

    override fun create(context: PathfindingContext, from: PathNode, to: PathNode, data: Int, cost: Double): Movement {
        val sprint = data == SPRINTING
        val launch = context.jumpSimulation.maxLaunchOffset
        return SimpleMovement(
            this,
            from,
            to,
            cost,
            sprintable = sprint,
            pathType = PathType.WALKABLE,
            requiresSprint = sprint,
            factory = { movement -> ParkourExecutor(movement, sprint, launch) }
        )
    }

    private fun candidatesTowards(
        context: PathfindingContext,
        from: PathNode,
        direction: Direction,
        furthest: Int,
        overhead: Int,
        out: MovementCandidates
    ) {
        var room = overhead

        for (distance in 1..furthest) {
            val x = from.x + direction.stepX * distance
            val z = from.z + direction.stepZ * distance

            if (distance >= MIN_DISTANCE) {
                this.offer(context, from, direction, x, z, distance, room, out)
            }

            if (context.findSupport(x, z, from.surface, from.surface - MIN_GAP_DEPTH) != PathfindingContext.NO_BLOCK) {
                return
            }

            room = this.roomOver(context, x, z, from.surface, room)
            if (room == NO_ROOM) {
                return
            }
        }
    }

    private fun offer(
        context: PathfindingContext,
        from: PathNode,
        direction: Direction,
        x: Int,
        z: Int,
        distance: Int,
        room: Int,
        out: MovementCandidates
    ) {
        val physics = context.jumpSimulation
        val ceiling = from.surface + physics.arc(false, room).apex
        val support = context.findSupport(x, z, ceiling, from.surface - physics.depth)
        if (support == PathfindingContext.NO_BLOCK) {
            return
        }

        val surface = context.getSurface(x, support, z)
        if (!MovementChecks.isStandable(context, x, z, surface)) {
            return
        }

        val landing = surface - from.surface
        if (this.lands(physics, false, room, distance, landing)) {
            out.acceptSurface(x, z, surface, this.cost(distance), WALKING)
            return
        }
        if (!context.settings.canSprint || !this.hasRunUp(from, direction)) {
            return
        }
        if (this.lands(physics, true, room, distance, landing)) {
            out.acceptSurface(x, z, surface, this.cost(distance), SPRINTING)
        }
    }

    private fun lands(
        physics: JumpPhysicsSimulation,
        sprinting: Boolean,
        room: Int,
        distance: Int,
        landing: Double
    ): Boolean {
        val arc = physics.arc(sprinting, room)
        val needed = physics.reachFor(distance, sprinting)
        val descent = arc.descentTo(landing)
        if (descent.isNaN() || needed > descent) {
            return false
        }
        val ascent = arc.ascentTo(landing)
        return !ascent.isNaN() && needed >= ascent
    }

    private fun roomOver(
        context: PathfindingContext,
        x: Int,
        z: Int,
        surface: Double,
        limit: Int
    ): Int {
        if (this.isClearTo(context, x, z, surface, limit)) {
            return limit
        }

        var low = 0
        var high = limit - 1
        var found = NO_ROOM
        while (low <= high) {
            val middle = (low + high) ushr 1
            if (this.isClearTo(context, x, z, surface, middle)) {
                found = middle
                low = middle + 1
            } else {
                high = middle - 1
            }
        }
        return found
    }

    private fun isClearTo(
        context: PathfindingContext,
        x: Int,
        z: Int,
        surface: Double,
        steps: Int
    ): Boolean {
        return MovementChecks.isColumnClear(context, x, z, surface, surface + context.jumpSimulation.roomAt(steps))
    }

    private fun hasRunUp(from: PathNode, direction: Direction): Boolean {
        val previous = from.previous ?: return false
        return from.x - previous.x == direction.stepX && from.z - previous.z == direction.stepZ
    }

    private fun cost(distance: Int): Double {
        return MovementCosts.SPRINT_JUMP_ONE_BLOCK * distance + RISK_PENALTY
    }
}
