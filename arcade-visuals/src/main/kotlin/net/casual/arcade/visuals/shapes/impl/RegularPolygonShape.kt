/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.shapes.impl

import net.casual.arcade.visuals.shapes.ShapePoints
import net.casual.arcade.visuals.utils.impl.SegmentedShapeIterator
import net.minecraft.world.phys.Vec3
import org.joml.Quaterniond
import org.joml.Vector3d
import kotlin.math.cos
import kotlin.math.sin

public class RegularPolygonShape(
    private val center: Vec3,
    private val radius: Double,
    private val vertices: Int
): ShapePoints {
    public val sideLength: Double
        get() = 2 * this.radius * sin(Math.PI / this.vertices)

    override fun iterator(pointsPerUnit: Double): Iterator<Vec3> {
        if (pointsPerUnit == 0.0 || this.vertices <= 1) {
            return PolygonIterator()
        }
        return SegmentedShapeIterator.of(Iterable { PolygonIterator() }, pointsPerUnit, true)
    }

    private inner class PolygonIterator: Iterator<Vec3> {
        private var index = 0

        override fun hasNext(): Boolean {
            return this.index < vertices
        }

        override fun next(): Vec3 {
            val angle = 2 * Math.PI * this.index++ / vertices
            return center.add(radius * cos(angle), 0.0, radius * sin(angle))
        }
    }

    public companion object {
        public fun createHorizontal(
            center: Vec3,
            radius: Double,
            vertices: Int
        ): RegularPolygonShape {
            return RegularPolygonShape(center, radius, vertices)
        }
    }
}