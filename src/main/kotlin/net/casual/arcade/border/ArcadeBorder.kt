package net.casual.arcade.border

import net.minecraft.world.level.border.BorderChangeListener
import net.minecraft.world.level.border.BorderStatus
import net.minecraft.world.level.border.WorldBorder
import net.minecraft.world.phys.shapes.VoxelShape
import org.joml.Vector2d


abstract class ArcadeBorder: WorldBorder() {




    protected abstract var state: BorderState


    protected abstract var centerState: CenterBorderState

    override fun tick() {
        this.state = this.state.update()
        this.centerState = this.centerState.update()
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
        return if (this.centerState != null) this.centerState.getCenter().x else 0.0
    }

    override fun getCenterZ(): Double {
        return if (this.centerState != null) this.centerState.getCenter().y else 0.0
    }


    override fun setCenter(x: Double, z: Double) {
        //Somehow this is still null when this would need to be called to init
        this.centerState = StillCenterBorderState(this.centerState.getCenter())

        this.state.onCenterChange()

        for (listener in this.listeners) {
            listener.onBorderCenterSet(this, x, z)
        }
    }

    fun setCenterLerped(x: Double, z: Double, realTime: Long) {
        this.centerState = MovingCenterBorderState(this, realTime)

        this.state.onCenterChange()

        for (listener in this.listeners) {
            listener.onBorderCenterSet(this, this.centerX, this.centerZ)
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
        this.centerState = StillCenterBorderState(this.centerState.getCenter())

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
        this.centerState = MovingCenterBorderState(this, time)

        for (borderChangeListener in listeners) {
            borderChangeListener.onBorderSizeLerping(this, start, end, time)
        }
    }

    override fun setAbsoluteMaxSize(size: Int) {

    }

    public override fun getListeners(): MutableList<BorderChangeListener> {
        return super.getListeners()
    }

    override fun getCollisionShape(): VoxelShape {
        return this.state.getCollisionShape()
    }

    fun isStationary(): Boolean {
        return this.status === BorderStatus.STATIONARY
    }
}