/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.pathfinding.execution

import net.casual.arcade.npc.FakePlayer
import net.casual.arcade.npc.ai.NPCInput
import net.casual.arcade.npc.pathfinding.movement.Movement
import net.minecraft.world.phys.Vec3

public open class WalkExecutor(
    protected val movement: Movement,
    protected val requireArrivalHeight: Boolean = false
): MovementExecutor {
    override fun tick(player: FakePlayer, input: NPCInput): MovementStatus {
        val target = this.movement.target
        if (this.hasArrived(player, target)) {
            return MovementStatus.Completed
        }
        this.steer(player, input, target)
        return MovementStatus.Moving
    }

    protected open fun steer(player: FakePlayer, input: NPCInput, target: Vec3) {
        MovementControls.moveTowards(player, input, target)

        if (this.shouldHoldOn(player, target)) {
            input.jump = true
        }
    }

    protected fun shouldHoldOn(player: FakePlayer, target: Vec3): Boolean {
        if (player.onGround() || target.y < player.y - ARRIVAL_HEIGHT) {
            return false
        }
        return player.onClimbable() || player.isInWater
    }

    protected open fun hasArrived(player: FakePlayer, target: Vec3): Boolean {
        if (this.requireArrivalHeight && player.y < target.y - ARRIVAL_HEIGHT) {
            return false
        }
        if (this.hasPassed(player, target)) {
            return true
        }
        return MovementControls.hasReached(player, target, ARRIVAL_RADIUS, ARRIVAL_HEIGHT)
    }

    protected fun hasPassed(player: FakePlayer, target: Vec3): Boolean {
        val start = this.movement.from.target
        val dx = target.x - start.x
        val dz = target.z - start.z
        val lengthSqr = dx * dx + dz * dz
        if (lengthSqr < MIN_LENGTH_SQR) {
            return false
        }
        val progress = ((player.x - start.x) * dx + (player.z - start.z) * dz) / lengthSqr
        return progress >= 1.0
    }

    public companion object {
        public const val ARRIVAL_RADIUS: Double = 0.35

        public const val ARRIVAL_HEIGHT: Double = 0.6

        private const val MIN_LENGTH_SQR = 1.0E-8
    }
}
