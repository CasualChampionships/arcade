/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.ai

import net.casual.arcade.npc.FakePlayer
import net.casual.arcade.utils.MathUtils.component1
import net.casual.arcade.utils.MathUtils.component2
import net.casual.arcade.utils.MathUtils.component3
import net.casual.arcade.utils.isOf
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.tags.BlockTags
import net.minecraft.util.Mth
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.control.Control
import net.minecraft.world.level.pathfinder.PathType
import net.minecraft.world.phys.Vec3
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

public open class NPCMoveControl(
    public val player: FakePlayer
): Control {
    protected var operation: Operation = Operation.WAIT
    protected var strafeForwards: Float = 0.0f
    protected var strafeRight: Float = 0.0f
    public var speedModifier: Double = 0.0
        protected set

    public var target: Vec3 = Vec3.ZERO
        private set
    public var jump: Boolean = false
        private set

    public var sprinting: Boolean = false
    public var sneaking: Boolean = false

    public fun hasWanted(): Boolean {
        return this.operation == Operation.MOVE_TO
    }

    public fun jump() {
        this.jump = true
    }

    public fun setTarget(wanted: Vec3, speed: Double) {
        this.target = wanted
        this.speedModifier = speed
        if (this.operation != Operation.JUMPING) {
            this.operation = Operation.MOVE_TO
        }
    }

    public fun strafe(forward: Float, strafe: Float) {
        this.operation = Operation.STRAFE
        this.strafeForwards = forward
        this.strafeRight = strafe
        this.speedModifier = 0.25
    }

    public fun tick() {
        when (this.operation) {
            Operation.STRAFE -> {
                val movementSpeed = this.player.getAttributeValue(Attributes.MOVEMENT_SPEED).toFloat()
                val speed = (this.speedModifier * movementSpeed).toFloat()
                var forward = this.strafeForwards
                var strafe = this.strafeRight
                var magnitude = Mth.sqrt(forward * forward + strafe * strafe)
                if (magnitude < 1.0f) {
                    magnitude = 1.0f
                }
                magnitude = speed / magnitude
                forward *= magnitude
                strafe *= magnitude
                val yawRadians = this.player.yRot * (Math.PI.toFloat() / 180.0f)
                val sinYaw = sin(yawRadians)
                val cosYaw = cos(yawRadians)
                val adjustedForward = forward * cosYaw - strafe * sinYaw
                val adjustedStrafe = strafe * cosYaw + forward * sinYaw
                if (!this.isWalkable(adjustedForward, adjustedStrafe)) {
                    this.strafeForwards = 1.0f
                    this.strafeRight = 0.0f
                }
                this.player.speed = speed
                this.player.zza = this.strafeForwards
                this.player.xxa = this.strafeRight
                this.operation = Operation.WAIT
            }
            Operation.MOVE_TO -> {
                this.operation = Operation.WAIT
                val delta = this.target.subtract(this.player.position())
                val distanceSq = delta.lengthSqr()
                if (distanceSq < MIN_SPEED_SQR) {
                    this.player.zza = 0.0f
                    return
                }
                val (dx, dy, dz) = delta
                val targetAngle = (atan2(dz, dx).toFloat() * 180.0f / Math.PI.toFloat()) - 90.0f
                this.player.yRot = this.rotlerp(this.player.yRot, targetAngle, 90.0f)
                val speed = (this.speedModifier * this.player.getAttributeValue(Attributes.MOVEMENT_SPEED)).toFloat() * 10
                this.player.zza = speed
                val blockPos = this.player.blockPosition()
                val blockState = this.player.level().getBlockState(blockPos)
                val voxelShape = blockState.getCollisionShape(this.player.level(), blockPos)
                if ((dy > this.player.maxUpStep() && dx * dx + dz * dz < max(1.0f, this.player.bbWidth).toDouble())
                    || (!voxelShape.isEmpty &&
                        this.player.y < voxelShape.max(Direction.Axis.Y) + blockPos.y &&
                        !blockState.isOf(BlockTags.DOORS) &&
                        !blockState.isOf(BlockTags.FENCES))
                ) {
                    this.jump()
                    this.operation = Operation.JUMPING
                }
            }
            Operation.JUMPING -> {
                val speed = (this.speedModifier * this.player.getAttributeValue(Attributes.MOVEMENT_SPEED)).toFloat()
                this.player.zza = speed
                if (this.player.onGround()) {
                    this.operation = Operation.WAIT
                }
            }
            else -> {
                this.player.zza = 0.0f
            }
        }

        this.player.isJumping = this.jump
        this.jump = false
    }

    private fun isWalkable(relativeX: Float, relativeZ: Float): Boolean {
        val evaluator = this.player.navigation.nodeEvaluator
        val pos = BlockPos.containing(
            this.player.x + relativeX,
            this.player.blockY.toDouble(),
            this.player.z + relativeZ
        )
        return evaluator.getPathType(this.player, pos) == PathType.WALKABLE
    }

    private fun rotlerp(sourceAngle: Float, targetAngle: Float, maximumChange: Float): Float {
        var angleDifference = Mth.wrapDegrees(targetAngle - sourceAngle)
        if (angleDifference > maximumChange) {
            angleDifference = maximumChange
        }
        if (angleDifference < -maximumChange) {
            angleDifference = -maximumChange
        }
        var newAngle = sourceAngle + angleDifference
        if (newAngle < 0.0f) {
            newAngle += 360.0f
        } else if (newAngle > 360.0f) {
            newAngle -= 360.0f
        }
        return newAngle
    }

    protected enum class Operation {
        WAIT,
        MOVE_TO,
        STRAFE,
        JUMPING
    }

    public companion object {
        public const val MIN_SPEED: Float = 5.0E-4F
        public const val MIN_SPEED_SQR: Float = 2.5000003E-7F
        protected const val MAX_TURN: Int = 90
    }
}
