/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.pathfinding.movement

import net.casual.arcade.npc.pathfinding.PathfindingContext
import net.minecraft.core.Direction
import net.minecraft.util.Mth
import net.minecraft.world.phys.AABB

/**
 * Geometry tests shared by the built-in movement types.
 *
 * Every height here is absolute and sub-block accurate.
 */
public object MovementChecks {
    private const val EPSILON = 1.0E-3

    public const val STEP_STAGES: Int = 2

    /**
     * Whether an NPC can walk from a surface at [fromHeight] onto the block at [toX], [toY], [toZ],
     * approaching along [direction], without jumping.
     *
     * @param direction The direction of travel.
     * @return `true` if the NPC steps up without jumping.
     */
    public fun canStepOnto(
        context: PathfindingContext,
        fromHeight: Double,
        toX: Int,
        toY: Int,
        toZ: Int,
        direction: Direction
    ): Boolean {
        val entry = context.getEntryHeight(toX, toY, toZ, direction.opposite)
        val stand = context.getSurface(toX, toY, toZ)
        if (stand == PathfindingContext.NO_SUPPORT) {
            return false
        }
        if (entry == PathfindingContext.NO_SUPPORT) {
            return stand - fromHeight <= context.stepHeight
        }
        val absoluteEntry = toY + entry
        return (absoluteEntry - fromHeight <= context.stepHeight) && (stand - absoluteEntry <= context.stepHeight)
    }

    /**
     * Whether a player fits while travelling between two standing positions.
     *
     * @param clearanceBelow How far below the higher of the two surfaces the corridor starts.
     * @param extraHeight Extra headroom to require, used by movements that leave the ground.
     * @return `true` if nothing collides with the NPC anywhere along the way.
     */
    public fun isCorridorClear(
        context: PathfindingContext,
        fromX: Int,
        fromZ: Int,
        fromHeight: Double,
        toX: Int,
        toZ: Int,
        toHeight: Double,
        clearanceBelow: Double = 0.0,
        extraHeight: Double = 0.0
    ): Boolean {
        val half = context.width / 2.0 - EPSILON
        val base = maxOf(fromHeight, toHeight) - clearanceBelow
        val box = AABB(
            minOf(fromX, toX) + 0.5 - half,
            base + EPSILON,
            minOf(fromZ, toZ) + 0.5 - half,
            maxOf(fromX, toX) + 0.5 + half,
            maxOf(fromHeight, toHeight) + context.height + extraHeight - EPSILON,
            maxOf(fromZ, toZ) + 0.5 + half
        )
        return context.level.noBlockCollision(context.player, box)
    }

    /**
     * Whether the column [x], [z] is free of collision between [fromHeight] and [toHeight].
     *
     * Used to check an player can fall without clipping an overhang on the way down.
     *
     * @return `true` if the column is clear over that range.
     */
    public fun isColumnClear(
        context: PathfindingContext,
        x: Int,
        z: Int,
        fromHeight: Double,
        toHeight: Double
    ): Boolean {
        val half = context.width / 2.0 - EPSILON
        val box = AABB(
            x + 0.5 - half,
            minOf(fromHeight, toHeight) + EPSILON,
            z + 0.5 - half,
            x + 0.5 + half,
            maxOf(fromHeight, toHeight) + context.height - EPSILON,
            z + 0.5 + half
        )
        return context.level.noBlockCollision(context.player, box)
    }

    /**
     * Whether the blocks a player's body would occupy in the column [x], [z], with its feet at
     * [surface], are all free of collision.
     *
     * @return `true` if the player's body fits.
     */
    public fun isBodyClear(context: PathfindingContext, x: Int, z: Int, surface: Double): Boolean {
        for (y in Mth.floor(surface)..Mth.floor(surface + context.height)) {
            if (!context.getBlock(x, y, z).isCollisionEmpty) {
                return false
            }
        }
        return true
    }

    /**
     * Whether an player may stand in the column [x], [z] on a surface at [height], given the
     * search's settings.
     *
     * @return `true` if standing there is allowed.
     */
    public fun isStandable(context: PathfindingContext, x: Int, z: Int, height: Double): Boolean {
        if (context.settings.avoidDamage && context.isDamaging(x, z, height)) {
            return false
        }
        if (!context.settings.canSwim && context.isSubmerged(x, z, height)) {
            return false
        }
        return true
    }

    /**
     * Whether the player can cut a corner.
     *
     * @param direction One of the two cardinal components of the diagonal.
     * @param other The other cardinal component.
     * @return `true` if the player can cut the corner.
     */
    public fun canCutCorner(
        context: PathfindingContext,
        fromX: Int,
        fromZ: Int,
        fromHeight: Double,
        direction: Direction,
        other: Direction
    ): Boolean {
        return this.isCornerOpen(context, fromX + direction.stepX, fromZ + direction.stepZ, fromHeight) &&
            this.isCornerOpen(context, fromX + other.stepX, fromZ + other.stepZ, fromHeight)
    }

    private fun isCornerOpen(context: PathfindingContext, x: Int, z: Int, height: Double): Boolean {
        return this.isColumnClear(context, x, z, height + context.stepHeight, height + context.stepHeight)
    }
}
