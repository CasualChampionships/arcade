/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.utils.impl

import net.minecraft.world.phys.Vec3
import kotlin.math.max

public class SegmentedShapeIterator private constructor(
    private val segments: List<SteppedShapeSegment>
) : Iterator<Vec3> {
    private var index = 0
    private var step = 0

    private val count: Int
        get() = this.segments.size

    override fun hasNext(): Boolean {
        return this.index < this.count && this.step <= this.segments[this.index].steps
    }

    override fun next(): Vec3 {
        val segment = this.segments[this.index]
        val delta = this.step.toDouble() / segment.steps
        val result = segment.start.lerp(segment.end, delta)

        this.step++
        if (this.step > segment.steps) {
            this.index++
            this.step = 0
        }

        return result
    }

    private data class SteppedShapeSegment(val start: Vec3, val end: Vec3, val steps: Int)

    public companion object {
        public fun of(segments: Iterable<ShapeSegment>, pointsPerUnit: Double): SegmentedShapeIterator {
            val stepped = segments.map { segmentToStepped(it.start, it.end, pointsPerUnit) }
            return SegmentedShapeIterator(stepped)
        }

        public fun of(points: Iterable<Vec3>, pointsPerUnit: Double, closed: Boolean): SegmentedShapeIterator {
            val segments = points.zipWithNext().mapTo(ArrayList()) { (a, b) ->
                this.segmentToStepped(a, b, pointsPerUnit)
            }
            if (closed && segments.size > 1) {
                val start = segments.last().end
                val end = segments.first().start
                segments.add(this.segmentToStepped(start, end, pointsPerUnit))
            }
            return SegmentedShapeIterator(segments)
        }

        private fun segmentToStepped(start: Vec3, end: Vec3, pointsPerUnit: Double): SteppedShapeSegment {
            val dist = start.distanceTo(end)
            val steps = max((dist * pointsPerUnit).toInt(), 1)
            return SteppedShapeSegment(start, end, steps)
        }
    }
}