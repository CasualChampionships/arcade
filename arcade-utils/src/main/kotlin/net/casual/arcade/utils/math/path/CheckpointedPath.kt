/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.math.path

import com.google.common.collect.ImmutableList
import com.mojang.serialization.Codec
import net.casual.arcade.utils.MathUtils.distanceToSegment
import net.minecraft.world.phys.Vec3

/**
 * This class represents a path represented by
 * a list of [Vec3]s.
 */
public class CheckpointedPath private constructor(
    private val checkpoints: List<Vec3>
): Iterable<Vec3> {
    /**
     * The length of the path.
     */
    public val length: Double

    init {
        var distance = 0.0
        for (i in 0 until this.checkpoints.lastIndex - 1) {
            distance += this.checkpoints[i].distanceTo(this.checkpoints[i + 1])
        }
        this.length = distance
    }

    /**
     * Gets the number of checkpoints in the path.
     *
     * @return The number of checkpoints.
     */
    public fun getCheckpointCount(): Int {
        return this.checkpoints.size
    }

    /**
     * Gets the checkpoint of a specific index.
     *
     * @throws IndexOutOfBoundsException Thrown when the index is out of bounds.
     */
    public fun getCheckpoint(index: Int): Vec3 {
        return this.checkpoints[index]
    }

    /**
     * Calculates the nearest checkpoint to a specified position.
     *
     * @param position The position.
     * @return The closest checkpoint to that position.
     */
    public fun calculateNearestCheckpoint(position: Vec3): Vec3 {
        return this.checkpoints.minBy(position::distanceToSqr)
    }

    /**
     * Calculates the nearest checkpoint index to a specified position.
     *
     * @param position The position.
     * @return The closest checkpoint index to that position.
     */
    public fun calculateNearestCheckpointIndex(position: Vec3): Int {
        var closestTargetIndex = 0
        var closestDistance = position.distanceToSqr(this.checkpoints[closestTargetIndex])
        for (i in 1..this.checkpoints.lastIndex) {
            val checkpoint = this.checkpoints[i]
            val distance = checkpoint.distanceToSqr(position)
            if (distance < closestDistance) {
                closestTargetIndex = i
                closestDistance = distance
            }
        }
        return closestTargetIndex
    }

    /**
     * Calculates the next checkpoint to target given a
     * specific position and tolerance.
     *
     * See [calculateNextCheckpointIndex] for how this is
     * calculated.
     *
     * @param position The starting position.
     * @param tolerance How close the [position] must be to
     * the checkpoint to count as visiting it.
     * @return The next checkpoint position.
     * @see calculateNextCheckpointIndex
     */
    public fun calculateNextCheckpoint(position: Vec3, tolerance: Double): Vec3 {
        return this.getCheckpoint(this.calculateNextCheckpointIndex(position, tolerance))
    }

    /**
     * This calculates the next checkpoint index given a
     * specific position and tolerance.
     *
     * This does *not* return the closest checkpoint but
     * instead returns the checkpoint that should be next
     * on the path.
     *
     * @param position The starting position.
     * @param tolerance How close the [position] must be to
     * the checkpoint to count as visiting it.
     * @return The next checkpoint index.
     * @see calculateNextCheckpoint
     */
    public fun calculateNextCheckpointIndex(position: Vec3, tolerance: Double): Int {
        var closestTargetIndex = 0
        var closestDistance = position.distanceTo(this.checkpoints[closestTargetIndex])
        for (i in 0 until this.checkpoints.lastIndex) {
            val start = this.checkpoints[i]
            val end = this.checkpoints[i + 1]
            if (start.closerThan(position, tolerance)) {
                // If we're at the start of a segment, we immediately know
                // that we must target this next segment
                return i + 1
            }

            val distance = position.distanceToSegment(start, end)
            if (distance < closestDistance) {
                closestDistance = distance
                closestTargetIndex = i + 1
            }
        }
        return closestTargetIndex
    }

    /**
     * This calculates the next checkpoint index given a
     * specific position, current index, and tolerance.
     *
     * This method essentially skips the calculations done
     * in the above method by only checking whether you
     * have reached the current checkpoint.
     *
     * @param position The starting position.
     * @param currentIndex The current targeted checkpoint index.
     * @param tolerance How close the [position] must be to
     * the checkpoint to count as visiting it.
     * @return The next checkpoint index.
     */
    public fun calculateNextCheckpointIndex(position: Vec3, currentIndex: Int, tolerance: Double): Int {
        val checkpoint = this.getCheckpoint(currentIndex)
        if (checkpoint.closerThan(position, tolerance) && currentIndex != this.checkpoints.lastIndex) {
            return currentIndex + 1
        }
        return currentIndex
    }

    override fun iterator(): Iterator<Vec3> {
        return this.checkpoints.iterator()
    }

    public companion object {
        public val CODEC: Codec<CheckpointedPath> = Vec3.CODEC.listOf(2, Int.MAX_VALUE)
            .xmap(::CheckpointedPath) { it.checkpoints }

        /**
         * Creates an instance of [CheckpointedPath],
         * the path must have at least 2 checkpoints.
         *
         * @param path The path.
         * @return The [CheckpointedPath] instance.
         */
        public fun create(path: List<Vec3>): CheckpointedPath {
            require(path.size < 2) { "Checkpointed path must have at least 2 points" }
            return CheckpointedPath(ImmutableList.copyOf(path))
        }
    }
}