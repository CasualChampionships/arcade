/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.shapes.impl

import net.casual.arcade.visuals.shapes.ShapePoints
import net.minecraft.world.phys.Vec3
import org.joml.Quaterniond
import org.joml.Quaterniondc
import org.joml.Vector3d

public class RotatedShape(
    private val wrapped: ShapePoints,
    private val center: Vec3,
    private val rotation: Quaterniondc
): ShapePoints {
    override fun iterator(pointsPerUnit: Double): Iterator<Vec3> {
        return WrappedIterator(this.wrapped.iterator(pointsPerUnit))
    }

    private inner class WrappedIterator(
        private val wrapped: Iterator<Vec3>
    ) : Iterator<Vec3> {

        override fun hasNext(): Boolean {
            return this.wrapped.hasNext()
        }

        override fun next(): Vec3 {
            val original = this.wrapped.next()

            val relative = Vector3d(
                original.x - center.x,
                original.y - center.y,
                original.z - center.z
            )

            rotation.transform(relative)
            return Vec3(
                relative.x + center.x,
                relative.y + center.y,
                relative.z + center.z
            )
        }
    }

    public companion object {
        public fun ShapePoints.rotated(center: Vec3, rotation: Quaterniondc): ShapePoints {
            if (this is RotatedShape && this.center == center) {
                val composed = this.rotation.mul(rotation, Quaterniond())
                return RotatedShape(this.wrapped, this.center, composed)
            }
            return RotatedShape(this, center, rotation)
        }
    }
}