package net.casual.arcade.gui.shapes

import net.minecraft.world.phys.Vec3
import org.joml.Quaterniond
import org.joml.Vector3d
import kotlin.math.cos
import kotlin.math.sin

public class Regular2DPolygonShape(
    private val center: Vec3,
    private val radius: Float,
    private val vertices: Int,
    private val rotation: Quaterniond
): ArcadeShape {
    override fun iterator(steps: Int): Iterator<Vec3> {
        if (steps == 1) {
            return PolygonIterator()
        }
        return SteppedVecIterator(steps, true, PolygonIterator())
    }

    private inner class PolygonIterator: Iterator<Vec3> {
        private var index = 0

        override fun hasNext(): Boolean {
            return this.index < vertices
        }

        override fun next(): Vec3 {
            val angle = 2 * Math.PI * this.index++ / vertices

            val point = Vector3d(radius * cos(angle), radius * sin(angle), 0.0)
            val rotated = rotation.transform(point)

            return center.add(rotated.x, rotated.y, rotated.z)
        }
    }

    public companion object {
        public fun createHorizontal(center: Vec3, radius: Float, vertices: Int): Regular2DPolygonShape {
            return Regular2DPolygonShape(center, radius, vertices, Quaterniond().rotateXYZ(Math.PI / 2, 0.0, 0.0))
        }
    }
}