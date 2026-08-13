/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.pathfinding.movement

import net.casual.arcade.npc.pathfinding.PathfindingContext
import net.casual.arcade.utils.registries.isOf
import net.minecraft.world.level.block.Blocks
import kotlin.math.floor

/**
 * Movement costs, all measured in ticks.
 */
public object MovementCosts {
    public const val GRAVITY: Double = 0.08
    public const val DRAG: Double = 0.98
    public const val WALK_ONE_BLOCK: Double = 20.0 / 4.317
    public const val SPRINT_ONE_BLOCK: Double = 20.0 / 5.612
    public const val SNEAK_ONE_BLOCK: Double = 20.0 / 1.3
    public const val SPRINT_JUMP_ONE_BLOCK: Double = 20.0 / 6.6
    public const val SWIM_ONE_BLOCK: Double = 20.0 / 1.6
    public const val SPRINT_SWIM_ONE_BLOCK: Double = 20.0 / 3.6
    public const val SWIM_UP_ONE_BLOCK: Double = 20.0 / 2.7
    public const val SWIM_DOWN_ONE_BLOCK: Double = 20.0 / 3.7
    public const val CLIMB_UP_ONE_BLOCK: Double = 20.0 / 2.35
    public const val CLIMB_DOWN_ONE_BLOCK: Double = 20.0 / 3.0
    public const val WALK_OFF_BLOCK: Double = WALK_ONE_BLOCK * 0.8
    public val JUMP_ONE_BLOCK: Double = ticksToFall(1.25) - ticksToFall(0.25)

    private const val SOUL_SAND = 2.0
    private const val HONEY = 2.5
    private const val SLIME = 1.25
    private const val ICE = 1.15
    private const val COBWEB = 5.0
    private const val POWDER_SNOW = 3.0
    private const val WATER = SWIM_ONE_BLOCK / WALK_ONE_BLOCK

    /**
     * How much slower than open ground the block at [x], [y], [z] is to walk across.
     *
     * @return A multiplier applied to the movement's base cost.
     */
    public fun surfaceMultiplier(context: PathfindingContext, x: Int, y: Int, z: Int): Double {
        val state = context.getBlock(x, y, z).state
        return when {
            state.isOf(Blocks.SOUL_SAND) -> SOUL_SAND
            state.isOf(Blocks.HONEY_BLOCK) -> HONEY
            state.isOf(Blocks.SLIME_BLOCK) -> SLIME
            state.isOf(Blocks.BLUE_ICE) || state.isOf(Blocks.PACKED_ICE) || state.isOf(Blocks.ICE) -> ICE
            else -> 1.0
        }
    }

    /**
     * How much slower than open air the block at [x], [y], [z] is to move through.
     *
     * @return A multiplier applied to the movement's base cost.
     */
    public fun withinMultiplier(context: PathfindingContext, x: Int, y: Int, z: Int): Double {
        val info = context.getBlock(x, y, z)
        return when {
            info.state.isOf(Blocks.COBWEB) -> COBWEB
            info.state.isOf(Blocks.POWDER_SNOW) -> POWDER_SNOW
            info.isWater -> WATER
            else -> 1.0
        }
    }

    /**
     * The cost of travelling [distance] blocks onto the surface of the block at [x], [y], [z].
     *
     * Accounts for both what is being walked on and what is being waded through.
     *
     * @param surface The absolute height the NPC ends up standing at.
     * @param distance How far the NPC travels, in blocks.
     * @param sprinting Whether the movement can be sprinted.
     * @return The movement's cost, in ticks.
     */
    public fun travel(
        context: PathfindingContext,
        x: Int,
        y: Int,
        z: Int,
        surface: Double,
        distance: Double,
        sprinting: Boolean = false
    ): Double {
        val feet = floor(surface).toInt()
        val base = if (sprinting) SPRINT_ONE_BLOCK else WALK_ONE_BLOCK
        return base * distance *
            this.surfaceMultiplier(context, x, y, z) *
            this.withinMultiplier(context, x, feet, z)
    }

    /**
     * The number of ticks an NPC takes to fall [distance] blocks from rest.
     *
     * @param distance The fall distance, in blocks.
     * @return The fall's duration, in ticks.
     */
    public fun ticksToFall(distance: Double): Double {
        if (distance <= 0.0) {
            return 0.0
        }
        var fallen = 0.0
        var velocity = 0.0
        var ticks = 0
        while (fallen < distance) {
            velocity = (velocity - GRAVITY) * DRAG
            val next = fallen - velocity
            if (next >= distance) {
                // Interpolate within the tick the player lands on
                return ticks + (distance - fallen) / (next - fallen)
            }
            fallen = next
            ticks++
        }
        return ticks.toDouble()
    }
}
