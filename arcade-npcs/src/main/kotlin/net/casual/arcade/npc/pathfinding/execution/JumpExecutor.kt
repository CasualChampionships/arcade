/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.pathfinding.execution

import net.casual.arcade.npc.FakePlayer
import net.casual.arcade.npc.ai.NPCInput
import net.casual.arcade.npc.pathfinding.movement.Movement
import net.minecraft.world.phys.Vec3

public open class JumpExecutor(
    movement: Movement
): WalkExecutor(movement, requireArrivalHeight = true) {
    override fun steer(player: FakePlayer, input: NPCInput, target: Vec3) {
        super.steer(player, input, target)

        if (this.shouldJump(player, target)) {
            input.jump = true
        }
    }

    protected open fun shouldJump(player: FakePlayer, target: Vec3): Boolean {
        if (!player.onGround()) {
            return false
        }
        if (player.y >= target.y - MIN_RISE) {
            return false
        }
        return MovementControls.horizontalDistanceTo(player, target) < JUMP_DISTANCE
    }

    public companion object {
        public const val JUMP_DISTANCE: Double = 1.0

        public const val MIN_RISE: Double = 0.1
    }
}
