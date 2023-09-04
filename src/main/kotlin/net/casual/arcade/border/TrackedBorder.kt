package net.casual.arcade.border


class TrackedBorder(size: Double, centerX: Double, centerZ: Double): ArcadeBorder() {
    
    override var centerState: CenterBorderState = StillCenterBorderState(centerX, centerZ)
    override var state: BorderState = StillBorderState(this, size)

    private var tracker: MultiLevelBorderTracker? = null

    override fun tick() {
        this.trackChanges { super.tick() }
    }

    override fun lerpSizeBetween(start: Double, end: Double, time: Long) {
        this.trackChanges { super.lerpSizeBetween(start, end, time) }
    }

    override fun setSize(size: Double) {
        this.trackChanges { super.setSize(size) }
    }

    internal fun setTracker(tracker: MultiLevelBorderTracker) {
        this.tracker = tracker
    }

    private inline fun trackChanges(block: TrackedBorder.() -> Unit) {
        val wasStationary = this.isStationary()
        block()
        if (!wasStationary && this.isStationary()) {
            this.tracker?.onBorderComplete(this)
        }
        if (wasStationary && !this.isStationary()) {
            this.tracker?.onBorderMove(this)
        }
    }
}