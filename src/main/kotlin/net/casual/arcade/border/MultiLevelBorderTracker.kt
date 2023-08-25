package net.casual.arcade.border

import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.border.BorderStatus
import java.util.LinkedList

class MultiLevelBorderTracker {
    private val tracking = HashMap<TrackedBorder, ServerLevel>()
    private val completed = HashSet<TrackedBorder>()

    private val listeners = ArrayList<MultiLevelBorderListener>()

    fun addLevelBorder(level: ServerLevel) {
        val border = level.worldBorder
        if (border !is TrackedBorder) {
            throw IllegalArgumentException("ServerLevel must have 'TrackedArcadeBorder' to be trackable")
        }
        border.setTracker(this)
        this.tracking[border] = level

        if (border.isStationary()) {
            this.completed.add(border)
        }
    }

    fun addListener(listener: MultiLevelBorderListener) {
        this.listeners.add(listener)
    }

    fun initialiseBorders() {
        this.listeners.forEach { it.onInitialiseBorder(this.tracking) }
    }

    fun getAllTracking(): Map<TrackedBorder, ServerLevel> {
        return this.tracking
    }

    internal fun onBorderComplete(border: TrackedBorder) {
        this.completed.add(border)
        this.listeners.forEach { it.onSingleBorderComplete(border, this.tracking[border]!!) }

        if (this.completed.size == tracking.size) {
            val incomplete = LinkedList<TrackedBorder>()
            for (complete in this.completed) {
                if (complete.status != BorderStatus.STATIONARY) {
                    incomplete.add(complete)
                }
            }
            for (tracked in incomplete) {
                this.completed.remove(tracked)
            }

            if (incomplete.isEmpty()) {
                this.listeners.forEach { it.onAllBordersComplete(this.tracking) }
            }
        }
    }

    internal fun onBorderMove(border: TrackedBorder) {
        this.completed.remove(border)
        this.listeners.forEach { it.onSingleBorderMove(border, this.tracking[border]!!) }
    }
}