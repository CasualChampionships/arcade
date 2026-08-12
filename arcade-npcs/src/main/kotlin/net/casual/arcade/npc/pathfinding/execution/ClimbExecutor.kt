/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.pathfinding.execution

import net.casual.arcade.npc.FakePlayer
import net.casual.arcade.npc.ai.NPCInput
import net.casual.arcade.npc.pathfinding.movement.Movement

public open class ClimbExecutor(
    protected val movement: Movement,
    protected val descending: Boolean
): MovementExecutor {
    private var loose: Int = 0

    override fun tick(player: FakePlayer, input: NPCInput): MovementStatus {
        val target = this.movement.target
        if (this.hasArrived(player, target.y)) {
            return MovementStatus.Completed
        }

        if (player.onClimbable() || player.onGround()) {
            this.loose = 0
        } else if (++this.loose > MAX_LOOSE_TICKS) {
            return MovementStatus.Failed
        }

        MovementControls.moveTowards(player, input, target, CENTRING_SPEED, MovementControls.NO_TURN_RATE)
        input.jump = !this.descending
        return MovementStatus.Moving
    }

    protected open fun hasArrived(player: FakePlayer, height: Double): Boolean {
        return if (this.descending) player.y <= height + ARRIVAL_HEIGHT else player.y >= height - ARRIVAL_HEIGHT
    }

    public companion object {
        public const val ARRIVAL_HEIGHT: Double = 0.05

        public const val CENTRING_SPEED: Float = 0.5F

        public const val MAX_LOOSE_TICKS: Int = 5
    }
}
