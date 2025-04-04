package net.casual.arcade.npc.ai

import net.casual.arcade.npc.FakePlayer
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.control.Control
import net.minecraft.world.phys.Vec3

public class NPCLookControl(
    private val player: FakePlayer
): Control {
    private var yMaxRotSpeed: Float = 0.0f
    private var xMaxRotAngle: Float = 0.0f
    private var lookAtCooldown: Int = 0
    private var wantedX: Double = 0.0
    private var wantedY: Double = 0.0
    private var wantedZ: Double = 0.0

    public fun setLookAt(lookVector: Vec3) {
        this.setLookAt(lookVector.x, lookVector.y, lookVector.z)
    }

    public fun setLookAt(entity: Entity) {
        this.setLookAt(entity.x, getWantedY(entity), entity.z)
    }

    public fun setLookAt(entity: Entity, deltaYaw: Float, deltaPitch: Float) {
        this.setLookAt(entity.x, getWantedY(entity), entity.z, deltaYaw, deltaPitch)
    }

    public fun setLookAt(x: Double, y: Double, z: Double) {
        this.setLookAt(x, y, z, 10.0F, 40.0F)
    }

    public fun setLookAt(x: Double, y: Double, z: Double, deltaYaw: Float, deltaPitch: Float) {
        this.wantedX = x
        this.wantedY = y
        this.wantedZ = z
        this.yMaxRotSpeed = deltaYaw
        this.xMaxRotAngle = deltaPitch
        this.lookAtCooldown = 2
    }

    public fun tick() {
        if (this.resetXRotOnTick()) {
            this.player.xRot = 0.0f
        }
        if (this.lookAtCooldown > 0) {
            this.lookAtCooldown--
            val yRotTarget = getYRotD()
            if (yRotTarget != null) {
                this.player.yHeadRot = this.rotateTowards(this.player.yHeadRot, yRotTarget, this.yMaxRotSpeed)
            }
            val xRotTarget = getXRotD()
            if (xRotTarget != null) {
                this.player.xRot = this.rotateTowards(this.player.xRot, xRotTarget, this.xMaxRotAngle)
            }
        } else {
            this.player.yHeadRot = this.rotateTowards(this.player.yHeadRot, this.player.yBodyRot, 10.0f)
        }
        this.clampHeadRotationToBody()
    }

    private fun clampHeadRotationToBody() {
        if (!this.player.navigation.isDone()) {
            this.player.yHeadRot = Mth.rotateIfNecessary(this.player.yHeadRot, this.player.yBodyRot, 75.0F)
        }
    }

    private fun resetXRotOnTick(): Boolean {
        return true
    }

    public fun isLookingAtTarget(): Boolean {
        return this.lookAtCooldown > 0
    }

    private fun getXRotD(): Float? {
        val dx = wantedX - player.x
        val dy = wantedY - player.eyeY
        val dz = wantedZ - player.z
        val horizontalDist = kotlin.math.sqrt(dx * dx + dz * dz)
        if (kotlin.math.abs(dy) <= 1.0E-5f || kotlin.math.abs(horizontalDist) <= 1.0E-5f) {
            return null
        }
        return -(Mth.atan2(dy, horizontalDist).toFloat() * 180.0f / Math.PI.toFloat())
    }

    private fun getYRotD(): Float? {
        val dx = wantedX - player.x
        val dz = wantedZ - player.z
        if (kotlin.math.abs(dx) <= 1.0E-5f || kotlin.math.abs(dz) <= 1.0E-5f) {
            return null
        }
        return -(Mth.atan2(dz, dx).toFloat() * 180.0f / Math.PI.toFloat()) - 90.0f
    }

    private fun getWantedY(entity: Entity): Double {
        if (entity is LivingEntity) {
            return entity.eyeY
        }
        val bb = entity.boundingBox
        return (bb.minY + bb.maxY) / 2.0
    }
}
