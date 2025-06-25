/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.border.tracker

import net.casual.arcade.border.CustomBorder
import net.casual.arcade.border.ducks.SerializableBorder
import net.casual.arcade.border.state.BorderState
import net.casual.arcade.border.state.CenterBorderState
import net.casual.arcade.border.state.StillBorderState
import net.casual.arcade.border.state.StillCenterBorderState
import net.minecraft.core.RegistryAccess
import net.minecraft.util.ProblemReporter
import net.minecraft.world.level.border.WorldBorder
import net.minecraft.world.level.storage.TagValueInput
import net.minecraft.world.level.storage.TagValueOutput
import java.util.*

public class TrackedBorder(size: Double, centerX: Double, centerZ: Double): CustomBorder() {
    override var centerState: CenterBorderState = StillCenterBorderState(centerX, centerZ)
    override var borderState: BorderState = StillBorderState(this, size)

    private var trackers = LinkedList<MultiLevelBorderTracker>()

    private var isTracking = false

    public constructor(border: WorldBorder): this(border.size, border.centerX, border.centerZ) {
        val output = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING)
        (border as SerializableBorder).`arcade$serialize`(output)
        val input = TagValueInput.create(ProblemReporter.DISCARDING, RegistryAccess.EMPTY, output.buildResult())
        this.`arcade$deserialize`(input)
    }

    override fun tick() {
        this.trackChanges { super.tick() }
    }

    override fun lerpSizeBetween(start: Double, end: Double, time: Long) {
        this.trackChanges { super.lerpSizeBetween(start, end, time) }
    }

    override fun lerpCenterBetween(fromX: Double, fromZ: Double, toX: Double, toZ: Double, time: Long) {
        this.trackChanges { super.lerpCenterBetween(fromX, fromZ, toX, toZ, time) }
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
        if (this.isTracking) {
            block()
            return
        }
        this.isTracking = true
        val previousSize = this.size
        val wasStationary = this.isStationary()
        block()
        if (wasStationary) {
            if (!this.isStationary()) {
                this.trackers.forEach { it.onBorderActive(this) }
            } else if (previousSize != this.size) {
                this.trackers.forEach { it.onBorderComplete(this) }
            }
        } else if (this.isStationary()) {
            this.trackers.forEach { it.onBorderComplete(this) }
        }
        this.isTracking = false
    }
}