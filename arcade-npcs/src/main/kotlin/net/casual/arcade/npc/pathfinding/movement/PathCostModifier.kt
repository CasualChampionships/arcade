/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.pathfinding.movement

import net.casual.arcade.npc.pathfinding.PathNode
import net.casual.arcade.npc.pathfinding.PathfindingContext
import net.casual.arcade.npc.pathfinding.navigation.PathNavigation

/**
 * Adjusts the cost of every edge a search considers, whatever movement type produced it.
 *
 * ```kotlin
 * val avoidObjective = PathCostModifier { _, _, _, to ->
 *     if (objective.contains(to.x, to.z)) 200.0 else 0.0
 * }
 * ```
 *
 * @see PathNavigation.costModifiers
 */
public fun interface PathCostModifier {
    /**
     * The extra cost of moving from [from] to [to].
     *
     * @param context The world the search is running against.
     * @param type The movement type that produced the edge.
     * @param from The node the edge starts at.
     * @param to The node the edge ends at.
     * @return Extra ticks to charge, or [Double.POSITIVE_INFINITY] to forbid the edge entirely.
     */
    public fun cost(
        context: PathfindingContext,
        type: MovementType,
        from: PathNode,
        to: PathNode
    ): Double
}
