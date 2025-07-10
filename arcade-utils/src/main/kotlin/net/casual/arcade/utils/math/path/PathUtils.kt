/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.math.path

import net.casual.arcade.utils.MathUtils.distanceToSegment
import net.casual.arcade.utils.MathUtils.horizontallyCloserThan
import net.casual.arcade.utils.MathUtils.verticallyCloserThan
import net.minecraft.world.level.pathfinder.Path
import net.minecraft.world.phys.Vec3

public fun Path.calculateNextNodeIndex(
    position: Vec3,
    horizontalTolerance: Double,
    verticalTolerance: Double
): Int? {
    val size = this.nodeCount
    if (size <= 0) {
        return null
    }
    if (size == 1) {
        return 0
    }

    var closestTargetIndex = 0
    var closestDistance = position.distanceTo(this.getNodePos(closestTargetIndex).bottomCenter)
    for (i in 0 until size - 1) {
        val start = this.getNodePos(i).bottomCenter
        val end = this.getNodePos(i + 1).bottomCenter
        val isAtStart = start.horizontallyCloserThan(position, horizontalTolerance)
            && start.verticallyCloserThan(position, verticalTolerance)
        if (isAtStart) {
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