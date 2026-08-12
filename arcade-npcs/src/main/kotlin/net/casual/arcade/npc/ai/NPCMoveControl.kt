/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.ai

import net.casual.arcade.npc.FakePlayer
import net.casual.arcade.npc.pathfinding.execution.MovementControls
import net.casual.arcade.npc.pathfinding.navigation.PathNavigation
import net.minecraft.core.BlockPos
import net.minecraft.util.Mth
import net.minecraft.world.entity.ai.control.Control
import net.minecraft.world.phys.Vec3
import kotlin.math.cos
import kotlin.math.sin

/**
 * Moves an NPC directly, without pathfinding.
 *
 * This only runs while [PathNavigation] has no path, so a walk target always takes precedence
 * over a manual one.
 */
@Suppress("MemberVisibilityCanBePrivate")
public open class NPCMoveControl(
    public val player: FakePlayer
): Control {
    protected var operation: Operation = Operation.Wait
    protected var strafeForwards: Float = 0.0f
    protected var strafeRight: Float = 0.0f

    public var speedModifier: Double = 0.0
        protected set

    public var target: Vec3 = Vec3.ZERO
        private set
    public var jump: Boolean = false
        private set

    public fun hasWanted(): Boolean {
        return this.operation == Operation.MoveTo
    }

    public fun jump() {
        this.jump = true
    }

    public fun setTarget(wanted: Vec3, speed: Double) {
        this.target = wanted
        this.speedModifier = speed
        if (this.operation != Operation.Jumping) {
            this.operation = Operation.MoveTo
        }
    }

    public fun strafe(forward: Float, strafe: Float) {
        this.operation = Operation.Strafe
        this.strafeForwards = forward
        this.strafeRight = strafe
        this.speedModifier = 0.25
    }

    public fun tick() {
        when (this.operation) {
            Operation.Strafe -> {
                val yawRadians = this.player.yRot * Mth.DEG_TO_RAD
                val sinYaw = sin(yawRadians)
                val cosYaw = cos(yawRadians)
                val adjustedForward = this.strafeForwards * cosYaw - this.strafeRight * sinYaw
                val adjustedStrafe = this.strafeRight * cosYaw + this.strafeForwards * sinYaw
                if (!this.isWalkable(adjustedForward, adjustedStrafe)) {
                    this.strafeForwards = 1.0f
                    this.strafeRight = 0.0f
                }
                this.player.input.setMoveVector(this.strafeRight, this.strafeForwards)
                this.operation = Operation.Wait
            }
            Operation.MoveTo -> {
                this.operation = Operation.Wait
                val delta = this.target.subtract(this.player.position())
                if (delta.horizontalDistanceSqr() < MIN_SPEED_SQR) {
                    this.player.input.setMoveVector(0.0f, 0.0f)
                    return
                }

                MovementControls.moveTowards(
                    this.player,
                    this.player.input,
                    this.target,
                    this.speedModifier.toFloat()
                )

                if (this.shouldJump(delta)) {
                    this.jump()
                    this.operation = Operation.Jumping
                }
            }
            Operation.Jumping -> {
                MovementControls.moveTowards(
                    this.player, this.player.input, this.target, this.speedModifier.toFloat()
                )
                if (this.player.onGround() || this.player.isInWater) {
                    this.operation = Operation.Wait
                }
            }
            else -> {
                this.player.input.setMoveVector(0.0f, 0.0f)
            }
        }

        this.player.input.jump = this.jump
        this.jump = false
    }

    private fun shouldJump(delta: Vec3): Boolean {
        if (!this.player.onGround()) {
            return false
        }
        if (delta.y > this.player.maxUpStep() && delta.horizontalDistanceSqr() < 1.0) {
            return true
        }
        return this.player.horizontalCollision && !this.player.minorHorizontalCollision
    }

    private fun isWalkable(relativeX: Float, relativeZ: Float): Boolean {
        val level = this.player.level()
        val offset = Vec3(relativeX.toDouble(), 0.0, relativeZ.toDouble())
        if (!level.noBlockCollision(this.player, this.player.boundingBox.move(offset))) {
            return false
        }
        val below = BlockPos.containing(
            this.player.x + relativeX,
            this.player.y - 0.5,
            this.player.z + relativeZ
        )
        return !level.getBlockState(below).getCollisionShape(level, below).isEmpty
    }

    protected enum class Operation {
        Wait,
        MoveTo,
        Strafe,
        Jumping
    }

    public companion object {
        public const val MIN_SPEED: Float = 5.0E-4F
        public const val MIN_SPEED_SQR: Float = 2.5000003E-7F
    }
}
