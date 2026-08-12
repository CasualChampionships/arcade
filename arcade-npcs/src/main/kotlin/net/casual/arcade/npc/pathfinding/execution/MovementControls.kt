/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.pathfinding.execution

import net.casual.arcade.npc.FakePlayer
import net.casual.arcade.npc.ai.NPCInput
import net.casual.arcade.utils.MathUtils.horizontalDistanceTo
import net.casual.arcade.utils.MathUtils.horizontallyCloserThan
import net.casual.arcade.utils.MathUtils.verticallyCloserThan
import net.minecraft.util.Mth
import net.minecraft.world.phys.Vec3
import kotlin.math.sqrt

public object MovementControls {
    public const val WALKING_TURN_RATE: Float = 30.0F
    public const val PRECISE_TURN_RATE: Float = 90.0F
    public const val NO_TURN_RATE: Float = 0.0F
    public const val MIN_TURN_DISTANCE: Double = 0.25

    public fun moveTowards(
        player: FakePlayer,
        input: NPCInput,
        target: Vec3,
        speed: Float = 1.0F,
        turnRate: Float = WALKING_TURN_RATE
    ) {
        val dx = target.x - player.x
        val dz = target.z - player.z
        val lengthSqr = dx * dx + dz * dz
        if (lengthSqr < MIN_DISTANCE_SQR) {
            input.setMoveVector(0.0F, 0.0F)
            return
        }

        if (lengthSqr >= MIN_TURN_DISTANCE * MIN_TURN_DISTANCE) {
            this.face(player, dx, dz, turnRate)
        }

        val length = sqrt(lengthSqr)
        this.setMoveDirection(player, input, dx / length, dz / length, speed)
    }

    public fun face(player: FakePlayer, dx: Double, dz: Double, turnRate: Float = WALKING_TURN_RATE) {
        if (dx * dx + dz * dz < MIN_DISTANCE_SQR) {
            return
        }
        val wanted = (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG).toFloat() - 90.0F
        player.yRot = Mth.approachDegrees(player.yRot, wanted, turnRate)
        player.yBodyRot = player.yRot
        player.yHeadRot = player.yRot
    }

    public fun setMoveDirection(player: FakePlayer, input: NPCInput, dx: Double, dz: Double, speed: Float = 1.0F) {
        this.resolveMoveDirection(player, input, dx, dz, speed)
        input.setMoveDirection(Vec3(dx, 0.0, dz), speed)
    }

    public fun resolveMoveDirection(
        player: FakePlayer,
        input: NPCInput,
        dx: Double,
        dz: Double,
        speed: Float = 1.0F
    ) {
        val radians = (player.yRot * Mth.DEG_TO_RAD).toDouble()
        val sin = Mth.sin(radians)
        val cos = Mth.cos(radians)
        val strafe = (dx * cos + dz * sin).toFloat()
        val forward = (dz * cos - dx * sin).toFloat()
        input.setMoveVector(strafe * speed, forward * speed)
    }

    public fun horizontalDistanceTo(player: FakePlayer, target: Vec3): Double {
        return player.position().horizontalDistanceTo(target)
    }

    public fun hasReached(
        player: FakePlayer,
        target: Vec3,
        horizontal: Double,
        vertical: Double
    ): Boolean {
        val position = player.position()
        return position.horizontallyCloserThan(target, horizontal) &&
            position.verticallyCloserThan(target, vertical)
    }

    private const val MIN_DISTANCE_SQR = 1.0E-8
}
