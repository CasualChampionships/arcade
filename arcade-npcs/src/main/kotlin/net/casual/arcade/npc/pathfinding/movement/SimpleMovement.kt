/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.pathfinding.movement

import net.casual.arcade.npc.pathfinding.PathNode
import net.casual.arcade.npc.pathfinding.execution.MovementExecutor
import net.minecraft.world.level.pathfinder.PathType

public class SimpleMovement(
    override val type: MovementType,
    override val from: PathNode,
    override val to: PathNode,
    override val cost: Double,
    override val sprintable: Boolean,
    override val pathType: PathType,
    override val requiresSprint: Boolean = false,
    private val factory: (Movement) -> MovementExecutor
): Movement {
    override fun createExecutor(): MovementExecutor {
        return this.factory.invoke(this)
    }

    override fun toString(): String {
        return "${this.type.id} ${this.from} -> ${this.to} (${this.cost} ticks)"
    }
}
