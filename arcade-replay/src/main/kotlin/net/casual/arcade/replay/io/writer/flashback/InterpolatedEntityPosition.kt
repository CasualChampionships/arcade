/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.io.writer.flashback

import net.minecraft.util.Mth
import net.minecraft.world.entity.PositionPath
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import kotlin.math.max

public class InterpolatedEntityPosition(
    private var current: ExactEntityPosition,
    private val interpolationSteps: Int
) {
    private val steps = ArrayDeque<Step>()
    private var target = this.current
    private var lastStep = this.current
    private var currentStepTicks = 0.0F
    private var remainingTicks = 0.0F
    private var speed = 1.0F

    public val position: ExactEntityPosition
        get() = this.current

    public val base: Vec3
        get() = this.target.position

    public val interpolating: Boolean
        get() = this.steps.isNotEmpty()

    public fun snap(position: ExactEntityPosition) {
        this.current = position
        this.target = position
        this.lastStep = position
        this.steps.clear()
        this.remainingTicks = 0.0F
        this.speed = 1.0F
    }

    public fun rotate(rotation: Vec2, headRot: Float, onGround: Boolean) {
        this.interpolateTo(null, rotation, headRot, onGround)
    }

    public fun move(path: PositionPath, rotation: Vec2?, headRot: Float?, onGround: Boolean) {
        this.interpolateTo(path, rotation, headRot, onGround)
    }

    public fun tick(): Boolean {
        if (this.steps.isEmpty()) {
            return false
        }

        var position: ExactEntityPosition? = null
        while (this.steps.isNotEmpty()) {
            val step = this.steps.first()
            if (this.currentStepTicks < step.ticks) {
                position = this.lastStep.lerp(step.target, this.currentStepTicks / step.ticks)
                break
            }
            this.currentStepTicks -= step.ticks
            this.lastStep = step.target
            this.steps.removeFirst()
        }
        this.current = (position ?: this.target).copy(onGround = this.current.onGround)

        var ticks = 1.0F
        val targetSpeed = max(this.remainingTicks / this.interpolationSteps, 1.0F)
        this.speed = Mth.lerp(1.0F / this.interpolationSteps, this.speed, targetSpeed)
        if (ticks * this.speed < this.remainingTicks) {
            ticks *= this.speed
        } else {
            ticks = this.remainingTicks
            this.speed = 1.0F
        }
        this.currentStepTicks += ticks
        this.remainingTicks -= ticks
        return true
    }

    private fun interpolateTo(path: PositionPath?, rotation: Vec2?, headRot: Float?, onGround: Boolean) {
        val end = ExactEntityPosition(
            path?.endPosition() ?: this.target.position,
            rotation ?: this.target.rotation,
            headRot ?: this.target.headRot,
            onGround
        )
        if (this.interpolationSteps <= 0) {
            this.snap(end)
            return
        }

        this.current = this.current.copy(onGround = onGround)
        if (this.steps.isNotEmpty() && this.target.isSamePositionAndRotation(end)) {
            this.target = end
            return
        }

        if (this.steps.isEmpty()) {
            this.lastStep = this.current
            this.currentStepTicks = 1.0F
        }

        if (path !is PositionPath.Stepped || end.position == this.target.position) {
            this.addStep(end, this.interpolationSteps)
        } else {
            val total = path.steps.sumOf { it.tickOffset }
            var offset = 0
            for (step in path.steps) {
                offset += step.tickOffset
                val rotated = this.target.lerp(end, offset.toFloat() / total)
                this.addStep(rotated.copy(position = step.position), step.tickOffset)
            }
        }
        this.target = end
    }

    private fun addStep(target: ExactEntityPosition, ticks: Int) {
        this.steps.add(Step(target, ticks))
        this.remainingTicks += ticks
    }

    private class Step(val target: ExactEntityPosition, val ticks: Int)
}
