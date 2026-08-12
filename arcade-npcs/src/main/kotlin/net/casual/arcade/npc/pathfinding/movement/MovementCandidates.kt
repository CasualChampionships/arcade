/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.pathfinding.movement

/**
 * Collects the edges a [MovementType] offers while the search expands a node.
 *
 * @see MovementType.candidates
 */
public interface MovementCandidates {
    /**
     * Offers an edge to the node standing on top of the block at [x], [y], [z].
     *
     * The search discards the edge if the destination is unreachable, already settled, or more
     * expensive than a route it already has, so callers do not need to check any of that.
     *
     * @param x The destination's supporting block X.
     * @param y The destination's supporting block Y.
     * @param z The destination's supporting block Z.
     * @param cost What this edge costs to traverse, in ticks.
     * @param data Recorded on the edge and handed back to [MovementType.create].
     */
    public fun accept(x: Int, y: Int, z: Int, cost: Double, data: Int = 0)

    /**
     * Offers an edge to the node whose feet rest at [surface] in the column [x], [z], for
     * movements that leave an player somewhere no block holds it up, such as partway up a ladder.
     *
     * Nothing about the destination is checked, since there is no block to check; the movement
     * type is responsible for making sure the player can be there.
     *
     * @param x The destination's column X.
     * @param z The destination's column Z.
     * @param surface The absolute height the player's feet end up at.
     * @param cost What this edge costs to traverse, in ticks.
     * @param data Recorded on the edge and handed back to [MovementType.create].
     */
    public fun acceptSurface(x: Int, z: Int, surface: Double, cost: Double, data: Int = 0)
}
