/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.math.path

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
 * @param tolerance The tolerance to be within each checkpoint.
 * @see CheckpointedPath
 */
public class CheckpointedPathFollower(
    position: Vec3,
    private val path: CheckpointedPath,
    private val tolerance: Double = 1.0
) {
    private var index = this.path.calculateNextCheckpointIndex(position, this.tolerance)

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
     * individually within the [tolerance], without skipping any.
     *
     * @param position The updated position.
     */
    public fun update(position: Vec3) {
        this.index = this.path.calculateNextCheckpointIndex(position, this.index, this.tolerance)
    }

    /**
     * Refreshes the [index] to be the next target checkpoint.
     *
     * This recalculates the next index, potentially skipping
     * checkpoint(s).
     *
     * @param position The updated position.
     */
    public fun refresh(position: Vec3) {
        this.index = this.path.calculateNextCheckpointIndex(position, this.tolerance)
    }
}