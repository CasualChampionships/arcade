/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.pathfinding.movement

import net.casual.arcade.npc.pathfinding.PathNode
import net.casual.arcade.npc.pathfinding.PathfindingContext
import net.minecraft.resources.Identifier

/**
 * A kind of movement an NPC can make, such as walking a block or dropping off a ledge.
 *
 * Implement this to teach an NPC something new; adding your type to an
 * [MovementTypeSet] makes the search plan through it.
 *
 * Movement types are split in two halves for performance. [candidates] runs in the search's
 * inner loop, once for every node the search expands, and must not allocate. [create] runs
 * only for the handful of edges that survive into the finished path.
 * ```kotlin
 * object DashMovementType: MovementType {
 *     override val id: Identifier = Identifier("example", "dash")
 *
 *     override fun candidates(context: PathfindingContext, from: PathNode, out: MovementCandidates) {
 *         for (direction in Direction.Plane.HORIZONTAL) {
 *             val x = from.x + direction.stepX * 5
 *             val z = from.z + direction.stepZ * 5
 *             val support = context.findSupport(x, z, from.surface, from.surface)
 *             if (support != PathfindingContext.NO_BLOCK) {
 *                 out.accept(x, support, z, DASH_COST, 0)
 *             }
 *         }
 *     }
 *
 *     override fun create(
 *         context: PathfindingContext,
 *         from: PathNode,
 *         to: PathNode,
 *         data: Int,
 *         cost: Double
 *     ): Movement {
 *         return DashMovement(from, to, cost)
 *     }
 * }
 * ```
 *
 * @see MovementTypeSet
 * @see Movement
 */
public interface MovementType {
    /**
     * Identifies this movement type in debug output and when mapping to vanilla path nodes.
     */
    public val id: Identifier

    /**
     * The lowest cost per block this type can ever produce.
     */
    public val minimumCostPerBlock: Double
        get() = MovementCosts.SPRINT_ONE_BLOCK

    /**
     * Offers every edge this type can produce leading away from [from].
     *
     * Called once per expanded node, so this should avoid allocating. Pass a [data] value to
     * [MovementCandidates.accept] to record which variant produced the edge; it is handed
     * back to [create] verbatim.
     *
     * @param context The world the search is running against.
     * @param from The node being expanded.
     * @param out Collects the edges.
     */
    public fun candidates(context: PathfindingContext, from: PathNode, out: MovementCandidates)

    /**
     * Builds the movement for an edge that made it into the finished path.
     *
     * @param context The world the search ran against.
     * @param from The node the movement starts at.
     * @param to The node the movement ends at.
     * @param data The value this type passed to [MovementCandidates.accept].
     * @param cost The cost this type passed to [MovementCandidates.accept].
     * @return The movement, which supplies the executor that performs it.
     */
    public fun create(context: PathfindingContext, from: PathNode, to: PathNode, data: Int, cost: Double): Movement
}
