/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.pathfinding.movement

import net.casual.arcade.npc.pathfinding.movement.types.*

/**
 * The movements an NPC is able to make.
 *
 * ```kotlin
 * player.navigation.movements = MovementTypeSet.DEFAULT + DashMovementType
 * ```
 *
 * @param types The movement types in this set, in the order they are offered edges.
 */
public class MovementTypeSet(types: Iterable<MovementType>): Iterable<MovementType> {
    /**
     * The movement types in this set.
     */
    public val types: List<MovementType> = types.distinctBy(MovementType::id)

    /**
     * The cheapest cost per block any type in this set can produce, used to estimate the cost
     * of reaching a target.
     */
    public val minimumCostPerBlock: Double = this.types
        .minOfOrNull(MovementType::minimumCostPerBlock)
        ?: MovementCosts.SPRINT_ONE_BLOCK

    override fun iterator(): Iterator<MovementType> {
        return this.types.iterator()
    }

    /**
     * A set with [type] added.
     *
     * @return The combined set.
     */
    public operator fun plus(type: MovementType): MovementTypeSet {
        return MovementTypeSet(this.types + type)
    }

    /**
     * A set with every type of [other] added.
     *
     * @return The combined set.
     */
    public operator fun plus(other: MovementTypeSet): MovementTypeSet {
        return MovementTypeSet(this.types + other.types)
    }

    /**
     * A set with [type] removed.
     *
     * @return The reduced set.
     */
    public operator fun minus(type: MovementType): MovementTypeSet {
        return MovementTypeSet(this.types.filter { it.id != type.id })
    }

    override fun toString(): String {
        return this.types.joinToString(prefix = "[", postfix = "]") { it.id.toString() }
    }

    public companion object {
        public val DEFAULT: MovementTypeSet = MovementTypeSet(listOf(
            TraverseMovementType,
            DiagonalMovementType,
            AscendMovementType,
            DescendMovementType,
            FallMovementType,
            ClimbMovementType,
            SwimMovementType,
            ParkourMovementType
        ))

        public val FLAT: MovementTypeSet = MovementTypeSet(listOf(
            TraverseMovementType,
            DiagonalMovementType
        ))
    }
}
