package net.casualuhc.arcade.border

import net.casualuhc.arcade.events.EventHandler
import net.casualuhc.arcade.events.border.CustomBorderStartEvent
import net.casualuhc.arcade.events.border.CustomBorderFinishEvent
import net.minecraft.world.level.border.BorderChangeListener
import net.minecraft.world.level.border.BorderStatus
import net.minecraft.world.level.border.WorldBorder

class CustomBorder: WorldBorder() {
    private var state: BorderState = StillBorderState(this, MAX_SIZE)
    private var centerX = 0.0
    private var centerZ = 0.0

    override fun tick() {

    }

    fun update() {
        val previous = this.status
        this.state = this.state.update()

        if (this.status != previous && previous == BorderStatus.STATIONARY) {
            val event = CustomBorderFinishEvent(this, false)
            EventHandler.broadcast(event)
        }
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
        return this.centerX
    }

    override fun getCenterZ(): Double {
        return this.centerZ
    }

    override fun setCenter(x: Double, z: Double) {
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
        val wasStationary = this.status == BorderStatus.STATIONARY

        this.state = StillBorderState(this, size)

        if (!wasStationary) {
            val event = CustomBorderFinishEvent(this, true)
            EventHandler.broadcast(event)
        }
    }

    override fun lerpSizeBetween(start: Double, end: Double, time: Long) {
        if (start == end) {
            this.size = end
            return
        }

        val wasStationary = this.status == BorderStatus.STATIONARY

        this.state = MovingBorderState(this, time, start, end)

        if (!wasStationary) {
            val event = CustomBorderFinishEvent(this, true)
            EventHandler.broadcast(event)
        }

        val event = CustomBorderStartEvent(this, start, end, time)
        EventHandler.broadcast(event)

        for (borderChangeListener in listeners) {
            borderChangeListener.onBorderSizeLerping(this, start, end, time)
        }
    }

    override fun setAbsoluteMaxSize(size: Int) {

    }

    public override fun getListeners(): MutableList<BorderChangeListener> {
        return super.getListeners()
    }
}