/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.shapes.impl

import net.casual.arcade.visuals.shapes.ShapePoints
import net.minecraft.world.phys.Vec3
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

public class EllipsoidShape(
    private val center: Vec3,
    private val radiusX: Double,
    private val radiusY: Double,
    private val radiusZ: Double
): ShapePoints {
    override fun iterator(pointsPerUnit: Double): Iterator<Vec3> {
        return EllipsoidIterator(pointsPerUnit)
    }

    private inner class EllipsoidIterator(
        pointsPerUnit: Double
    ) : Iterator<Vec3> {
        private val avgCircumference = 2 * Math.PI * ((radiusX + radiusY + radiusZ) / 3.0)
        private val totalPoints = (this.avgCircumference * pointsPerUnit).toInt().coerceAtLeast(10)

        private val latSteps = sqrt(this.totalPoints.toDouble()).toInt().coerceAtLeast(2)
        private val lonSteps = this.totalPoints / this.latSteps

        private var lat = 0
        private var lon = 0

        override fun hasNext(): Boolean {
            return this.lat < this.latSteps
        }

        override fun next(): Vec3 {
            if (!this.hasNext()) {
                throw NoSuchElementException()
            }

            val phi = Math.PI * (this.lat.toDouble() / (this.latSteps - 1))
            val theta = 2 * Math.PI * (this.lon.toDouble() / this.lonSteps)

            val x = radiusX * sin(phi) * cos(theta)
            val y = radiusY * cos(phi)
            val z = radiusZ * sin(phi) * sin(theta)

            val point = center.add(x, y, z)

            this.lon++
            if (this.lon >= this.lonSteps) {
                this.lon = 0
                this.lat++
            }

            return point
        }
    }

    public companion object {
        public fun sphere(center: Vec3, radius: Double): EllipsoidShape {
            return EllipsoidShape(center, radius, radius, radius)
        }
    }
}