/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.visuals.shapes.impl

import net.casual.arcade.virtual.visuals.shapes.ShapePoints
import net.casual.arcade.virtual.visuals.shapes.segment.SegmentedShapeIterator
import net.casual.arcade.virtual.visuals.shapes.segment.ShapeSegment
import net.minecraft.world.phys.Vec3

public class LineShape(
    private val start: Vec3,
    private val end: Vec3
): ShapePoints {
    private val segment = ShapeSegment(this.start, this.end)

    override fun iterator(pointsPerUnit: Double): Iterator<Vec3> {
        return SegmentedShapeIterator.of(listOf(this.segment), pointsPerUnit)
    }
}