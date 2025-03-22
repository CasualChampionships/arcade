package net.casual.arcade.visuals.shapes

import net.minecraft.world.phys.Vec3
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

public class SphereShape(
    private val center: Vec3,
    private val radius: Double
): ShapePoints {
    override fun iterator(steps: Int): Iterator<Vec3> {
        return SphereIterator(steps * 4)
    }

    private inner class SphereIterator(private val steps: Int) : Iterator<Vec3> {
        private var index = 0

        override fun hasNext(): Boolean {
            return this.index < this.steps
        }

        override fun next(): Vec3 {
            if (!hasNext()) {
                throw NoSuchElementException("No more points available")
            }

            if (this.steps == 1) {
                this.index++
                return Vec3(center.x + radius, center.y, center.z)
            }

            val i = this.index++
            val y = 1 - (i.toDouble() / (this.steps - 1)) * 2
            val r = sqrt(1 - y * y)
            val theta = GOLDEN_ANGLE * i
            val x = cos(theta) * r
            val z = sin(theta) * r

            return Vec3(center.x + radius * x, center.y + radius * y, center.z + radius * z)
        }
    }

    private companion object {
        val GOLDEN_ANGLE = Math.PI * (3 - sqrt(5.0))
    }
}