/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.border

import net.casual.arcade.border.ducks.SerializableBorder
import net.casual.arcade.border.state.*
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.minecraft.world.level.border.BorderChangeListener
import net.minecraft.world.level.border.BorderStatus
import net.minecraft.world.level.border.WorldBorder
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import net.minecraft.world.phys.shapes.VoxelShape

public abstract class CustomBorder: WorldBorder(), SerializableBorder {
    protected abstract var borderState: BorderState
    protected abstract var centerState: CenterBorderState

    override fun tick() {
        this.borderState = this.borderState.update()
        this.centerState = this.centerState.update()
    }

    final override fun getStatus(): BorderStatus {
        return this.borderState.getStatus()
    }

    public fun getCenterStatus(): CenterBorderStatus {
        return this.centerState.getStatus()
    }

    final override fun getMinX(): Double {
        return this.borderState.getMinX()
    }

    final override fun getMinZ(): Double {
        return this.borderState.getMinZ()
    }

    final override fun getMaxX(): Double {
        return this.borderState.getMaxX()
    }

    final override fun getMaxZ(): Double {
        return this.borderState.getMaxZ()
    }

    final override fun getCenterX(): Double {
        return this.centerState.getCenterX()
    }

    final override fun getCenterZ(): Double {
        return this.centerState.getCenterZ()
    }

    override fun setCenter(x: Double, z: Double) {
        this.setCenterUntracked(x, z)
    }

    public fun setCenterUntracked(x: Double, z: Double) {
        this.centerState = StillCenterBorderState(x, z)
        this.changeCenter(x, z)
    }

    public open fun lerpCenterBetween(fromX: Double, fromZ: Double, toX: Double, toZ: Double, time: Long) {
        this.lerpCenterBetweenUntracked(fromX, fromZ, toX, toZ, time)
    }

    public open fun lerpCenterBetweenUntracked(fromX: Double, fromZ: Double, toX: Double, toZ: Double, time: Long) {
        this.centerState = MovingCenterBorderState(this, fromX, fromZ, toX, toZ, time)
    }

    public fun lerpCenterTo(x: Double, z: Double, duration: MinecraftTimeDuration) {
        this.lerpCenterBetween(this.centerX, this.centerZ, x, z, duration.milliseconds.toLong())
    }

    final override fun getSize(): Double {
        return this.borderState.getSize()
    }

    final override fun getLerpRemainingTime(): Long {
        return this.borderState.getLerpRemainingTime()
    }

    final override fun getLerpTarget(): Double {
        return this.borderState.getLerpTarget()
    }

    final override fun getLerpSpeed(): Double {
        return this.borderState.getLerpSpeed()
    }

    override fun setSize(size: Double) {
        this.setSizeUntracked(size)
    }

    public fun setSizeUntracked(size: Double) {
        this.borderState = StillBorderState(this, size)

        for (borderChangeListener in listeners) {
            borderChangeListener.onBorderSizeSet(this, size)
        }
    }

    override fun lerpSizeBetween(start: Double, end: Double, time: Long) {
        this.lerpSizeBetweenUntracked(start, end, time)
    }

    public fun lerpSizeBetweenUntracked(start: Double, end: Double, time: Long) {
        if (start == end) {
            this.size = end
            return
        }

        this.borderState = MovingBorderState(this, time, start, end)

        for (borderChangeListener in listeners) {
            borderChangeListener.onBorderSizeLerping(this, start, end, time)
        }
    }

    public fun lerpSizeBetween(start: Double, end: Double, duration: MinecraftTimeDuration) {
        this.lerpSizeBetween(start, end, duration.milliseconds.toLong())
    }

    override fun setAbsoluteMaxSize(size: Int) {

    }

    public final override fun getListeners(): MutableList<BorderChangeListener> {
        return super.getListeners()
    }

    final override fun getCollisionShape(): VoxelShape {
        return this.borderState.getCollisionShape()
    }

    public fun isStationary(): Boolean {
        return this.status === BorderStatus.STATIONARY && this.getCenterStatus() === CenterBorderStatus.STATIONARY
    }

    internal fun changeCenter(x: Double, z: Double) {
        this.borderState.onCenterChange()

        for (listener in this.listeners) {
            listener.onBorderCenterSet(this, x, z)
        }
    }

    override fun `arcade$serialize`(output: ValueOutput) {
        output.putDouble("center_x", this.centerX)
        output.putDouble("center_z", this.centerZ)
        output.putDouble("size", this.size)
        output.putDouble("damage_safe_zone", this.damageSafeZone)
        output.putDouble("damage_per_block", this.damagePerBlock)
        output.putLong("lerp_time", this.lerpRemainingTime)
        output.putDouble("lerp_target", this.lerpTarget)
        output.putInt("warning_blocks", this.warningBlocks)
        output.putInt("warning_time", this.warningTime)
        output.putLong("center_lerp_time", this.centerState.getLerpRemainingTime())
        output.putDouble("center_lerp_target_x", this.centerState.getTargetCenterX())
        output.putDouble("center_lerp_target_z", this.centerState.getTargetCenterZ())
    }

    override fun `arcade$deserialize`(input: ValueInput) {
        val settings = DEFAULT_SETTINGS
        this.damagePerBlock = input.getDoubleOr("damage_per_block", settings.damagePerBlock)
        this.damageSafeZone = input.getDoubleOr("damage_safe_zone", settings.safeZone)
        this.warningBlocks = input.getIntOr("warning_blocks", settings.warningBlocks)
        this.warningTime = input.getIntOr("warning_time", settings.warningTime)
        val remaining = input.getLongOr("lerp_time", settings.sizeLerpTime)
        val size = input.getDoubleOr("size", settings.size)
        if (remaining > 0L) {
            this.lerpSizeBetweenUntracked(size, input.getDoubleOr("lerp_target", settings.sizeLerpTarget), remaining)
        } else {
            this.setSizeUntracked(size)
        }

        val centerRemaining = input.getLongOr("center_lerp_time", 0)
        val centerX = input.getDoubleOr("center_x", 0.0)
        val centerZ = input.getDoubleOr("center_z", 0.0)
        if (centerRemaining > 0L) {
            this.lerpCenterBetweenUntracked(
                centerX,
                centerZ,
                input.getDoubleOr("center_lerp_target_x", 0.0),
                input.getDoubleOr("center_lerp_target_z", 0.0),
                centerRemaining
            )
        } else {
            this.setCenterUntracked(centerX, centerZ)
        }
    }
}