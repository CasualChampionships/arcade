/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.shapes.impl

import net.casual.arcade.visuals.shapes.ShapePoints
import net.casual.arcade.visuals.utils.impl.ShapeSegment
import net.casual.arcade.visuals.utils.impl.SegmentedShapeIterator
import net.minecraft.world.phys.Vec3
import kotlin.math.cos
import kotlin.math.sin

public class ArrowShape(
    private val tip: Vec3,
    private val left: Vec3,
    private val right: Vec3,
    private val back: Vec3
): ShapePoints {
    private val points = listOf(
        ShapeSegment(this.back, this.tip),
        ShapeSegment(this.tip, this.right),
        ShapeSegment(this.tip, this.left)
    )

    override fun iterator(pointsPerUnit: Double): Iterator<Vec3> {
        return SegmentedShapeIterator.of(this.points, pointsPerUnit)
    }

    override fun toString(): String {
        return "Tip: $tip, Left: $left, Right: $right, Back $back"
    }

    public companion object {
        public fun createHorizontalCentred(x: Int, y: Double, z: Int, scale: Double, rotation: Double): ArrowShape {
            return createHorizontal(x + 0.5, y, z + 0.5, scale, rotation)
        }

        public fun createHorizontal(x: Double, y: Double, z: Double, scale: Double, rotation: Double): ArrowShape {
            val size = scale / 2.0

            val tip = Vec3(size * sin(rotation), 0.0, size * cos(rotation))
            val left = Vec3(tip.x - size * cos(rotation), 0.0, tip.z + size * sin(rotation))
            val right = Vec3(tip.x + size * cos(rotation), 0.0, tip.z - size * sin(rotation))
            val back = tip.reverse()

            return ArrowShape(
                tip.add(x, y, z),
                left.scale(0.5).add(x, y, z),
                right.scale(0.5).add(x, y, z),
                back.add(x, y, z)
            )
        }
    }
}