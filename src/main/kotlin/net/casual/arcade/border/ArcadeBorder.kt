package net.casual.arcade.border

import net.casual.arcade.border.state.*
import net.casual.arcade.ducks.`Arcade$SerializableBorder`
import net.casual.arcade.scheduler.MinecraftTimeDuration
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.border.BorderChangeListener
import net.minecraft.world.level.border.BorderStatus
import net.minecraft.world.level.border.WorldBorder
import net.minecraft.world.phys.shapes.VoxelShape

public abstract class ArcadeBorder: WorldBorder(), `Arcade$SerializableBorder` {
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

    override fun `arcade$serialize`(): CompoundTag {
        // This essentially calls super.serialize() (WorldBorer#serialize())
        val compound =  (this as `Arcade$SerializableBorder`).`arcade$serialize`()
        compound.putLong("center_lerp_time", this.centerState.getLerpRemainingTime())
        compound.putDouble("center_lerp_target_x", this.centerState.getTargetCenterX())
        compound.putDouble("center_lerp_target_z", this.centerState.getTargetCenterZ())
        return compound
    }

    override fun `arcade$deserialize`(compound: CompoundTag) {
        this.damagePerBlock = compound.getDouble("damage_per_block")
        this.damageSafeZone = compound.getDouble("damage_safe_zone")
        this.warningBlocks = compound.getInt("warning_blocks")
        this.warningTime = compound.getInt("warning_time")
        val remaining = compound.getLong("lerp_time")
        val size = compound.getDouble("size")
        if (remaining > 0L) {
            this.lerpSizeBetweenUntracked(size, compound.getDouble("lerp_target"), remaining)
        } else {
            this.setSizeUntracked(size)
        }

        val centerRemaining = compound.getLong("center_lerp_time")
        val centerX = compound.getDouble("center_x")
        val centerZ = compound.getDouble("center_z")
        if (centerRemaining > 0L) {
            this.lerpCenterBetweenUntracked(
                centerX,
                centerZ,
                compound.getDouble("center_lerp_target_x"),
                compound.getDouble("center_lerp_target_z"),
                compound.getLong("center_lerp_time")
            )
        } else {
            this.setCenterUntracked(centerX, centerZ)
        }
    }
}