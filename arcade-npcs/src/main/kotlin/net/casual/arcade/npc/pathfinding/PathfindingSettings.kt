/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.pathfinding

import net.casual.arcade.npc.pathfinding.movement.MovementType
import net.casual.arcade.npc.pathfinding.movement.types.ClimbMovementType
import net.casual.arcade.npc.pathfinding.movement.types.ParkourMovementType

/**
 * Configures what an NPC is allowed to do while pathfinding, and how hard the search tries.
 *
 * These are read by the built-in [MovementType]s; custom movement types carry their own
 * configuration on their own instances.
 */
public class PathfindingSettings {
    /**
     * Whether the player can sprint.
     */
    public var canSprint: Boolean = true

    /**
     * Whether the player will try to swim.
     */
    public var canSwim: Boolean = false

    /**
     * Whether ladders, vines and scaffolding may be climbed.
     *
     * @see ClimbMovementType
     */
    public var canClimb: Boolean = true

    /**
     * Whether gaps may be jumped across.
     *
     * @see ParkourMovementType
     */
    public var canParkour: Boolean = true

    /**
     * A ceiling on how far the player will try to parkour.
     *
     * @see JumpPhysicsSimulation
     */
    public var maxParkourDistance: Int = 5

    /**
     * Whether damaging blocks may be walked over.
     */
    public var avoidDamage: Boolean = true

    /**
     * The greatest drop, in blocks, that may be planned.
     */
    public var maxFallDistance: Int = 3

    /**
     * The greatest rise, in blocks, that a single jump may clear.
     *
     * A player's jump apex is roughly `1.25`; the default leaves headroom for the horizontal
     * travel that happens during the jump.
     */
    public var maxJumpHeight: Double = 1.125

    /**
     * Weights the A* heuristic.
     *
     * `1.0` searches for an optimal path; higher values reach the target sooner but may return
     * a worse path.
     */
    public var heuristicWeight: Double = 1.2

    /**
     * The greatest number of nodes a single search may expand.
     */
    public var maxVisitedNodes: Int = 8192

    /**
     * How far, in blocks, the search may wander from its starting position.
     */
    public var maxPathLength: Float = 64.0F

    /**
     * Copies these settings.
     *
     * @return The copied settings.
     */
    public fun copy(): PathfindingSettings {
        val copy = PathfindingSettings()
        copy.canSprint = this.canSprint
        copy.canSwim = this.canSwim
        copy.canClimb = this.canClimb
        copy.canParkour = this.canParkour
        copy.maxParkourDistance = this.maxParkourDistance
        copy.avoidDamage = this.avoidDamage
        copy.maxFallDistance = this.maxFallDistance
        copy.maxJumpHeight = this.maxJumpHeight
        copy.heuristicWeight = this.heuristicWeight
        copy.maxVisitedNodes = this.maxVisitedNodes
        copy.maxPathLength = this.maxPathLength
        return copy
    }
}
