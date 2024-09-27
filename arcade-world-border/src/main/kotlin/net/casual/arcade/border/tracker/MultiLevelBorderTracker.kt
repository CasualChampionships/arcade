package net.casual.arcade.border.tracker

import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.border.WorldBorder
import java.util.*

public class MultiLevelBorderTracker {
    private val tracking = HashMap<TrackedBorder, ServerLevel>()
    private val completed = HashSet<TrackedBorder>()

    private val listeners = ArrayList<MultiLevelBorderListener>()

    public fun addLevelBorder(level: ServerLevel) {
        val border = this.castToTracked(level.worldBorder)
        border.addTracker(this)
        this.tracking[border] = level

        if (border.isStationary()) {
            this.completed.add(border)
        }
    }

    public fun removeLevelBorder(level: ServerLevel) {
        val border = this.castToTracked(level.worldBorder)
        border.removeTracker(this)
        this.tracking.remove(border)

        this.completed.remove(border)
    }

    public fun addListener(listener: MultiLevelBorderListener) {
        this.listeners.add(listener)
    }

    public fun initialiseBorders() {
        this.listeners.forEach { it.onInitialiseBorder(this.getAllTracking()) }
    }

    public fun getAllTracking(): Map<TrackedBorder, ServerLevel> {
        return Collections.unmodifiableMap(this.tracking)
    }

    internal fun onBorderComplete(border: TrackedBorder) {
        this.completed.add(border)
        this.listeners.forEach { it.onSingleBorderComplete(border, this.tracking[border]!!) }

        if (this.completed.size == this.tracking.size) {
            val incomplete = LinkedList<TrackedBorder>()
            for (complete in this.completed) {
                if (!complete.isStationary()) {
                    incomplete.add(complete)
                }
            }
            for (tracked in incomplete) {
                this.completed.remove(tracked)
            }

            if (incomplete.isEmpty()) {
                this.listeners.forEach { it.onAllBordersComplete(this.getAllTracking()) }
            }
        }
    }

    internal fun onBorderActive(border: TrackedBorder) {
        this.completed.remove(border)
        this.listeners.forEach { it.onSingleBorderActive(border, this.tracking[border]!!) }
    }

    private fun castToTracked(border: WorldBorder): TrackedBorder {
        if (border !is TrackedBorder) {
            throw IllegalArgumentException("ServerLevel must have 'TrackedBorder' to be trackable")
        }
        return border
    }
}