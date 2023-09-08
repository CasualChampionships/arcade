package net.casual.arcade.border

import net.casual.arcade.border.state.BorderState
import net.casual.arcade.border.state.CenterBorderState
import net.casual.arcade.border.state.StillBorderState
import net.casual.arcade.border.state.StillCenterBorderState
import java.util.*

class TrackedBorder(size: Double, centerX: Double, centerZ: Double): ArcadeBorder() {
    override var centerState: CenterBorderState = StillCenterBorderState(centerX, centerZ)
    override var borderState: BorderState = StillBorderState(this, size)

    private var trackers = LinkedList<MultiLevelBorderTracker>()

    override fun tick() {
        this.trackChanges { super.tick() }
    }

    override fun lerpSizeBetween(start: Double, end: Double, time: Long) {
        this.trackChanges { super.lerpSizeBetween(start, end, time) }
    }

    override fun lerpCenterTo(x: Double, z: Double, realTime: Long) {
        this.trackChanges { super.lerpCenterTo(x, z, realTime) }
    }

    override fun setSize(size: Double) {
        this.trackChanges { super.setSize(size) }
    }

    override fun setCenter(x: Double, z: Double) {
        this.trackChanges { super.setCenter(x, z) }
    }

    internal fun addTracker(tracker: MultiLevelBorderTracker) {
        this.trackers.add(tracker)
    }

    internal fun removeTracker(tracker: MultiLevelBorderTracker) {
        this.trackers.remove(tracker)
    }

    private inline fun trackChanges(block: TrackedBorder.() -> Unit) {
        val wasStationary = this.isStationary()
        block()
        if (!wasStationary && this.isStationary()) {
            this.trackers.forEach { it.onBorderComplete(this) }
        }
        if (wasStationary && !this.isStationary()) {
            this.trackers.forEach { it.onBorderActive(this) }
        }
    }
}