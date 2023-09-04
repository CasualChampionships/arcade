package net.casual.arcade.border

import net.minecraft.world.level.border.BorderChangeListener
import net.minecraft.world.level.border.BorderStatus
import net.minecraft.world.level.border.WorldBorder

abstract class ArcadeBorder: WorldBorder() {
    private var centerX = 0.0
    private var centerZ = 0.0

    private var targetCenterX = 0.0
    private var targetCenterZ = 0.0



    protected abstract var state: BorderState

    override fun tick() {
        this.state = this.state.update()
    }

    override fun getStatus(): BorderStatus {
        return this.state.getStatus()
    }

    override fun getMinX(): Double {
        return this.state.getMinX()
    }

    override fun getMinZ(): Double {
        return this.state.getMinZ()
    }

    override fun getMaxX(): Double {
        return this.state.getMaxX()
    }

    override fun getMaxZ(): Double {
        return this.state.getMaxZ()
    }

    override fun getCenterX(): Double {
        return if (this.state != null) this.state.getCenterX(this.centerX, this.getTargetCenterX()) else this.centerX
    }

    override fun getCenterZ(): Double {
        return if (this.state != null) this.state.getCenterZ(this.centerZ, this.getTargetCenterZ()) else this.centerZ
    }
    fun getTargetCenterX(): Double {
        return this.targetCenterX
    }
    fun getTargetCenterZ(): Double {
        return this.targetCenterZ
    }

    fun setCenterLerp(x: Double, z: Double) {
        this.targetCenterX = x
        this.targetCenterZ = z
        this.state.onCenterChange()

        //Update listeners in border state update method.

    }
    override fun setCenter(x: Double, z: Double) {
        this.targetCenterX = x
        this.targetCenterZ = z

        this.centerX = x
        this.centerZ = z

        this.state.onCenterChange()

        for (listener in this.listeners) {
            listener.onBorderCenterSet(this, x, z)
        }
    }

    override fun getSize(): Double {
        return this.state.getSize()
    }

    override fun getLerpRemainingTime(): Long {
        return this.state.getLerpRemainingTime()
    }

    override fun getLerpTarget(): Double {
        return this.state.getLerpTarget()
    }

    override fun getLerpSpeed(): Double {
        return this.state.getLerpSpeed()
    }

    override fun setSize(size: Double) {
        this.state = StillBorderState(this, size)

        for (borderChangeListener in listeners) {
            borderChangeListener.onBorderSizeSet(this, size)
        }
    }

    override fun lerpSizeBetween(start: Double, end: Double, time: Long) {
        if (start == end) {
            this.size = end
            return
        }

        this.state = MovingBorderState(this, time, start, end)

        for (borderChangeListener in listeners) {
            borderChangeListener.onBorderSizeLerping(this, start, end, time)
        }
    }

    override fun setAbsoluteMaxSize(size: Int) {

    }

    public override fun getListeners(): MutableList<BorderChangeListener> {
        return super.getListeners()
    }

    fun isStationary(): Boolean {
        return this.status === BorderStatus.STATIONARY
    }
}