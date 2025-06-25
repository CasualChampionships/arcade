/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.ai

import net.casual.arcade.npc.FakePlayer
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.control.Control
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import kotlin.math.abs
import kotlin.math.sqrt

public class NPCLookControl(
    private val player: FakePlayer
): Control {
    private var lookAtCooldown: Int = 0
    private var target: Target? = null

    public fun setLookAt(lookVector: Vec3) {
        this.setLookAt(lookVector.x, lookVector.y, lookVector.z)
    }

    public fun setLookAt(entity: Entity) {
        this.setLookAt(entity.x, this.getWantedY(entity), entity.z)
    }

    public fun setLookAt(entity: Entity, deltaYaw: Float, deltaPitch: Float) {
        this.setLookAt(entity.x, this.getWantedY(entity), entity.z, deltaYaw, deltaPitch)
    }

    public fun setLookAt(x: Double, y: Double, z: Double) {
        this.setLookAt(x, y, z, 10.0F, 40.0F)
    }

    public fun setLookAt(x: Double, y: Double, z: Double, deltaYaw: Float, deltaPitch: Float) {
        this.target = Target(Vec3(x, y, z), Vec2(deltaPitch, deltaYaw))
        this.lookAtCooldown = 2
    }

    public fun tick() {
        val target = this.target ?: return

        if (this.lookAtCooldown > 0) {
            this.lookAtCooldown--
            val yRotTarget = this.getYRotD(target)
            if (yRotTarget != null) {
                this.player.yRot = this.rotateTowards(this.player.yRot, yRotTarget, target.rotationMaxes.y)
            }
            val xRotTarget = this.getXRotD(target)
            if (xRotTarget != null) {
                this.player.xRot = this.rotateTowards(this.player.xRot, xRotTarget, target.rotationMaxes.x)
            }
        } else {
            this.target = null
        }
        this.clampHeadRotationToBody()
    }

    private fun clampHeadRotationToBody() {
        if (!this.player.navigation.isDone()) {
            this.player.yRot = Mth.rotateIfNecessary(this.player.yRot, this.player.yBodyRot, 75.0F)
        }
    }

    public fun isLookingAtTarget(): Boolean {
        return this.lookAtCooldown > 0
    }

    private fun getXRotD(target: Target): Float? {
        val dx = target.position.x - this.player.x
        val dy = target.position.y - this.player.eyeY
        val dz = target.position.z - this.player.z
        val horizontalDist = sqrt(dx * dx + dz * dz)
        if (abs(dy) <= Mth.EPSILON || abs(horizontalDist) <= Mth.EPSILON) {
            return null
        }
        return -(Mth.atan2(dy, horizontalDist).toFloat() * Mth.RAD_TO_DEG)
    }

    private fun getYRotD(target: Target): Float? {
        val dx = target.position.x - this.player.x
        val dz = target.position.z - this.player.z
        if (abs(dx) <= Mth.EPSILON || abs(dz) <= Mth.EPSILON) {
            return null
        }
        return (Mth.atan2(dz, dx).toFloat() * Mth.RAD_TO_DEG) - 90.0f
    }

    private fun getWantedY(entity: Entity): Double {
        if (entity is LivingEntity) {
            return entity.eyeY
        }
        val bb = entity.boundingBox
        return (bb.minY + bb.maxY) / 2.0
    }

    private data class Target(val position: Vec3, val rotationMaxes: Vec2)
}
