/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.pathfinding.execution

import net.casual.arcade.npc.FakePlayer
import net.casual.arcade.npc.ai.NPCInput
import net.casual.arcade.npc.pathfinding.movement.Movement
import net.minecraft.world.phys.Vec3
import kotlin.math.abs

public open class SwimExecutor(
    movement: Movement
): WalkExecutor(movement) {
    override fun steer(player: FakePlayer, input: NPCInput, target: Vec3) {
        if (!this.isVertical()) {
            MovementControls.moveTowards(player, input, target)
        }
        this.holdDepth(player, input, target.y)
    }

    protected fun isVertical(): Boolean {
        return this.movement.from.x == this.movement.to.x && this.movement.from.z == this.movement.to.z
    }

    protected open fun holdDepth(player: FakePlayer, input: NPCInput, height: Double) {
        val difference = height - player.y
        if (difference > DEPTH_TOLERANCE) {
            input.jump = true
        } else if (difference < -DEPTH_TOLERANCE && this.canSink(player)) {
            player.input.shift = true
//            player.sinkInWater()
        }
    }

    protected fun canSink(player: FakePlayer): Boolean {
        return player.isInWater && !player.onGround()
    }

    override fun hasArrived(player: FakePlayer, target: Vec3): Boolean {
        if (this.isVertical()) {
            return abs(player.y - target.y) < ARRIVAL_DEPTH
        }
        if (this.hasPassed(player, target)) {
            return true
        }
        return MovementControls.hasReached(player, target, ARRIVAL_RADIUS, ARRIVAL_DEPTH)
    }

    public companion object {
        public const val DEPTH_TOLERANCE: Double = 0.2

        public const val ARRIVAL_DEPTH: Double = 0.35
    }
}
