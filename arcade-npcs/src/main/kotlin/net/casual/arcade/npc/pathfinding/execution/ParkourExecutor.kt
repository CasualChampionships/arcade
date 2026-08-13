/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.pathfinding.execution

import net.casual.arcade.npc.FakePlayer
import net.casual.arcade.npc.ai.NPCInput
import net.casual.arcade.npc.pathfinding.movement.Movement
import net.minecraft.util.Mth
import net.minecraft.world.phys.Vec3
import kotlin.math.sqrt

public open class ParkourExecutor(
    movement: Movement,
    protected val requiresSprint: Boolean,
    protected val launchOffset: Double
): WalkExecutor(movement) {
    private var launched = false

    override fun start(player: FakePlayer) {
        this.launched = false
    }

    override fun tick(player: FakePlayer, input: NPCInput): MovementStatus {
        if (this.launched && player.onGround() && !this.hasArrived(player, this.movement.target)) {
            return MovementStatus.Failed
        }
        return super.tick(player, input)
    }

    override fun hasArrived(player: FakePlayer, target: Vec3): Boolean {
        if (!player.onGround()) {
            return false
        }
        if (!this.launched) {
            return false
        }
        return MovementControls.hasReached(player, target, LANDING_RADIUS, ARRIVAL_HEIGHT)
    }

    override fun steer(player: FakePlayer, input: NPCInput, target: Vec3) {
        val start = this.movement.from.target
        val dx = target.x - start.x
        val dz = target.z - start.z

        MovementControls.face(player, dx, dz, MovementControls.PRECISE_TURN_RATE)
        val length = sqrt(dx * dx + dz * dz)
        MovementControls.setMoveDirection(player, input, dx / length, dz / length)

        if (!this.launched && this.shouldJump(player, start, dx, dz, length)) {
            input.jump = true
            this.launched = true
        }
    }

    protected open fun shouldJump(player: FakePlayer, start: Vec3, dx: Double, dz: Double, length: Double): Boolean {
        if (!player.onGround()) {
            return false
        }
        if (this.requiresSprint && !player.isSprinting) {
            return false
        }

        val travelled = ((player.x - start.x) * dx + (player.z - start.z) * dz) / length
        val speed = player.deltaMovement.horizontalDistance()
        if (travelled + speed <= this.launchOffset) {
            return false
        }
        val wanted = (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG).toFloat() - 90.0F
        return Mth.degreesDifferenceAbs(player.yRot, wanted) < LAUNCH_ANGLE
    }

    public companion object {
        private const val LAUNCH_ANGLE = 5.0F
        private const val LANDING_RADIUS = 0.6
    }
}
