/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.pathfinding.movement.types

import net.casual.arcade.npc.pathfinding.PathNode
import net.casual.arcade.npc.pathfinding.PathfindingContext
import net.casual.arcade.npc.pathfinding.execution.MovementExecutor
import net.casual.arcade.npc.pathfinding.execution.SwimExecutor
import net.casual.arcade.npc.pathfinding.movement.*
import net.casual.arcade.utils.arcade
import net.minecraft.core.Direction
import net.minecraft.resources.Identifier
import net.minecraft.util.Mth
import net.minecraft.world.level.pathfinder.PathType

public object SwimMovementType: MovementType {
    override val id: Identifier = arcade("swim")

    public const val ACROSS: Int = 0

    public const val UP: Int = 1

    public const val DOWN: Int = 2

    public const val ENTER: Int = 3

    public const val SUBMERGED_PENALTY: Double = 4.0

    override val minimumCostPerBlock: Double
        get() = MovementCosts.SPRINT_SWIM_ONE_BLOCK

    override fun candidates(context: PathfindingContext, from: PathNode, out: MovementCandidates) {
        if (!context.settings.canSwim) {
            return
        }

        val feet = Mth.floor(from.surface)
        if (context.getBlock(from.x, feet, from.z).isWater) {
            this.acrossCandidates(context, from, feet, out)
            this.verticalCandidates(context, from, feet, out)
        } else {
            this.enterCandidates(context, from, feet, out)
        }
    }

    override fun create(context: PathfindingContext, from: PathNode, to: PathNode, data: Int, cost: Double): Movement {
        return SimpleMovement(
            this, from, to, cost, sprintable = data == ACROSS, pathType = PathType.WATER, factory = ::SwimExecutor
        )
    }

    private fun acrossCandidates(
        context: PathfindingContext,
        from: PathNode,
        feet: Int,
        out: MovementCandidates
    ) {
        val surface = feet.toDouble()
        for (direction in Direction.Plane.HORIZONTAL) {
            val x = from.x + direction.stepX
            val z = from.z + direction.stepZ
            if (!this.canFloat(context, x, z, surface)) {
                continue
            }
            if (!MovementChecks.isCorridorClear(context, from.x, from.z, surface, x, z, surface)) {
                continue
            }
            out.acceptSurface(x, z, surface, this.acrossCost(context, x, z, surface), ACROSS)
        }
    }

    private fun verticalCandidates(
        context: PathfindingContext,
        from: PathNode,
        feet: Int,
        out: MovementCandidates
    ) {
        val above = (feet + 1).toDouble()
        if (this.canFloat(context, from.x, from.z, above)) {
            out.acceptSurface(from.x, from.z, above, MovementCosts.SWIM_UP_ONE_BLOCK, UP)
        }

        val below = (feet - 1).toDouble()
        if (this.canFloat(context, from.x, from.z, below)) {
            val cost = MovementCosts.SWIM_DOWN_ONE_BLOCK + this.submergedPenalty(context, from.x, from.z, below)
            out.acceptSurface(from.x, from.z, below, cost, DOWN)
        }
    }

    private fun enterCandidates(context: PathfindingContext, from: PathNode, feet: Int, out: MovementCandidates) {
        val lowest = feet - context.settings.maxFallDistance
        for (direction in Direction.Plane.HORIZONTAL) {
            val x = from.x + direction.stepX
            val z = from.z + direction.stepZ
            for (y in feet downTo lowest) {
                val block = context.getBlock(x, y, z)
                if (!block.isWater) {
                    if (!block.isCollisionEmpty) {
                        break
                    }
                    continue
                }
                if (!this.canFloat(context, x, z, y.toDouble())) {
                    break
                }
                if (context.findSupport(x, z, y.toDouble(), y.toDouble()) != PathfindingContext.NO_BLOCK) {
                    break
                }

                val drop = from.surface - y
                val cost = MovementCosts.SWIM_ONE_BLOCK + MovementCosts.ticksToFall(drop) + this.submergedPenalty(context, x, z, y.toDouble())
                out.acceptSurface(x, z, y.toDouble(), cost, ENTER)
                break
            }
        }
    }

    private fun canFloat(context: PathfindingContext, x: Int, z: Int, surface: Double): Boolean {
        return context.getBlock(x, Mth.floor(surface), z).isWater &&
            MovementChecks.isStandable(context, x, z, surface) &&
            MovementChecks.isBodyClear(context, x, z, surface)
    }

    private fun acrossCost(context: PathfindingContext, x: Int, z: Int, surface: Double): Double {
        val submerged = context.isSubmerged(x, z, surface)
        val base = if (submerged && context.settings.canSprint) {
            MovementCosts.SPRINT_SWIM_ONE_BLOCK
        } else {
            MovementCosts.SWIM_ONE_BLOCK
        }
        return base + if (submerged) SUBMERGED_PENALTY else 0.0
    }

    private fun submergedPenalty(context: PathfindingContext, x: Int, z: Int, surface: Double): Double {
        return if (context.isSubmerged(x, z, surface)) SUBMERGED_PENALTY else 0.0
    }
}
