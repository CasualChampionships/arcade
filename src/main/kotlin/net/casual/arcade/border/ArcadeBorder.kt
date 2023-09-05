package net.casual.arcade.border

import net.casual.arcade.border.state.*
import net.minecraft.world.level.border.BorderChangeListener
import net.minecraft.world.level.border.BorderStatus
import net.minecraft.world.level.border.WorldBorder
import net.minecraft.world.phys.shapes.VoxelShape

abstract class ArcadeBorder: WorldBorder() {
    protected abstract var borderState: BorderState
    protected abstract var centerState: CenterBorderState

    override fun tick() {
        this.borderState = this.borderState.update()
        this.centerState = this.centerState.update()
    }

    final override fun getStatus(): BorderStatus {
        return this.borderState.getStatus()
    }

    fun getCenterStatus(): CenterBorderStatus {
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
        this.centerState = StillCenterBorderState(x, z)
        this.changeCenter(x, z)
    }

    open fun lerpCenterTo(x: Double, z: Double, realTime: Long) {
        this.centerState = MovingCenterBorderState(this, this.centerState.getCenterX(), this.centerState.getCenterZ(), x, z, realTime)
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
        this.borderState = StillBorderState(this, size)

        for (borderChangeListener in listeners) {
            borderChangeListener.onBorderSizeSet(this, size)
        }
    }

    override fun lerpSizeBetween(start: Double, end: Double, time: Long) {
        if (start == end) {
            this.size = end
            return
        }

        this.borderState = MovingBorderState(this, time, start, end)

        for (borderChangeListener in listeners) {
            borderChangeListener.onBorderSizeLerping(this, start, end, time)
        }
    }

    override fun setAbsoluteMaxSize(size: Int) {

    }

    public final override fun getListeners(): MutableList<BorderChangeListener> {
        return super.getListeners()
    }

    final override fun getCollisionShape(): VoxelShape {
        return this.borderState.getCollisionShape()
    }

    fun isStationary(): Boolean {
        return this.status === BorderStatus.STATIONARY && this.getCenterStatus() === CenterBorderStatus.STATIONARY
    }

    internal fun changeCenter(x: Double, z: Double) {
        this.borderState.onCenterChange()

        for (listener in this.listeners) {
            listener.onBorderCenterSet(this, x, z)
        }
    }
}