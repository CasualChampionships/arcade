/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.math.path

import net.casual.arcade.utils.math.path.CheckpointedPath.ProximityChecker
import net.minecraft.world.phys.Vec3

/**
 * Utility class that keeps track of the next
 * index of a [CheckpointedPath] that it's following.
 *
 * Calling [update] or [refresh] periodically to
 * update the [target] position on the path.
 *
 * @param position The starting position.
 * @param path The path to follow.
 * @param checker The checker to determine whether a
 * position is within a checkpoint.
 * @see CheckpointedPath
 */
public class CheckpointedPathFollower(
    position: Vec3,
    public val path: CheckpointedPath,
    private val checker: ProximityChecker = ProximityChecker.within(1.0)
) {
    public var index: Int = this.path.calculateNextCheckpointIndex(position, this.checker)

    /**
     * The target position.
     */
    public val target: Vec3
        get() = this.path.getCheckpoint(this.index)

    /**
     * Checks whether the [position] has reached the next checkpoint,
     * and updates the [index] accordingly.
     *
     * Calling this assumes that you want to visit each checkpoint
     * individually within the [checker], without skipping any.
     *
     * @param position The updated position.
     * @return Whether the next checkpoint changed.
     */
    public fun update(position: Vec3): Status {
        val previous = this.index
        this.index = this.path.calculateNextCheckpointIndex(position, this.index, this.checker)
        return this.getStatus(previous, position)
    }

    /**
     * Refreshes the [index] to be the next target checkpoint.
     *
     * This recalculates the next index, potentially skipping
     * checkpoint(s).
     *
     * @param position The updated position.
     * @return Whether the next checkpoint changed.
     */
    public fun refresh(position: Vec3): Status {
        val previous = this.index
        this.index = this.path.calculateNextCheckpointIndex(position, this.checker)
        return this.getStatus(previous, position)
    }

    private fun getStatus(previous: Int, position: Vec3): Status {
        if (previous != this.index) {
            return Status.Updated
        }
        if (this.path.isLastCheckpoint(this.index) && this.checker.isWithinCheckpoint(position, this.target)) {
            return Status.Finished
        }
        return Status.Noop
    }

    public enum class Status {
        Noop, Updated, Finished
    }
}