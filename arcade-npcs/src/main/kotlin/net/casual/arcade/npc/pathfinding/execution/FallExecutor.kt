/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.pathfinding.execution

import net.casual.arcade.npc.FakePlayer
import net.casual.arcade.npc.ai.NPCInput
import net.casual.arcade.npc.pathfinding.movement.Movement
import net.minecraft.world.phys.Vec3

public open class FallExecutor(
    movement: Movement
): WalkExecutor(movement) {
    override fun hasArrived(player: FakePlayer, target: Vec3): Boolean {
        if (!player.onGround()) {
            return false
        }
        return MovementControls.hasReached(player, target, LANDING_RADIUS, ARRIVAL_HEIGHT)
    }

    override fun steer(player: FakePlayer, input: NPCInput, target: Vec3) {
        val start = this.movement.from.target
        MovementControls.face(player, target.x - start.x, target.z - start.z)

        MovementControls.moveTowards(player, input, target, this.travelSpeed(player), MovementControls.NO_TURN_RATE)
    }

    protected open fun travelSpeed(player: FakePlayer): Float {
        return if (player.onGround()) 1.0F else AIRBORNE_SPEED
    }

    public companion object {
        public const val LANDING_RADIUS: Double = 0.5

        public const val AIRBORNE_SPEED: Float = 0.6F
    }
}
