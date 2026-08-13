/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.pathfinding.movement

import net.casual.arcade.npc.pathfinding.Path
import net.casual.arcade.npc.pathfinding.PathNode
import net.casual.arcade.npc.pathfinding.execution.MovementExecutor
import net.minecraft.world.level.pathfinder.PathType
import net.minecraft.world.phys.Vec3

/**
 * A single step of a finished path, and the thing that knows how to perform it.
 *
 * @see MovementType.create
 */
public interface Movement {
    /**
     * The type that produced this movement.
     */
    public val type: MovementType

    /**
     * Where the movement starts.
     */
    public val from: PathNode

    /**
     * Where the movement ends.
     */
    public val to: PathNode

    /**
     * What this movement was costed at, in ticks.
     */
    public val cost: Double

    /**
     * Whether the NPC may sprint while performing this movement.
     */
    public val sprintable: Boolean
        get() = false

    /**
     * Whether this movement is impossible without sprinting.
     *
     * The executor keeps sprint held for these even where it would otherwise slow down, such as
     * on the last movement of a path.
     */
    public val requiresSprint: Boolean
        get() = false

    /**
     * The position the NPC travels to.
     */
    public val target: Vec3
        get() = this.to.target

    /**
     * How this movement appears when the path is converted for the vanilla debug renderer and
     * brain memory.
     *
     * @see Path.asVanillaPath
     */
    public val pathType: PathType
        get() = PathType.WALKABLE

    /**
     * Creates the executor that performs this movement.
     *
     * @return A fresh executor.
     */
    public fun createExecutor(): MovementExecutor
}
