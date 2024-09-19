package net.casual.arcade.visuals.shapes

import net.minecraft.world.phys.Vec3
import kotlin.math.cos
import kotlin.math.sin

public class ArrowShape(
    private val tip: Vec3,
    private val left: Vec3,
    private val right: Vec3,
    private val back: Vec3
): ShapePoints {
    override fun iterator(steps: Int): Iterator<Vec3> {
        return ArrowIterator(steps)
    }

    override fun toString(): String {
        return "Tip: ${this.tip}, Left: ${this.left}, Right: ${this.right}, Back ${this.back}"
    }

    private inner class ArrowIterator(
        val steps: Int
    ): Iterator<Vec3> {
        private val delta = 1.0 / this.steps

        private var from = back
        private var current = 0
        private var step = 0

        override fun hasNext(): Boolean {
            return this.current != 2 || this.step <= this.steps
        }

        override fun next(): Vec3 {
            if (this.step > this.steps) {
                when (this.current) {
                    0 -> {
                        this.current = 1
                        this.from = left
                    }
                    1 -> {
                        this.current = 2
                        this.from = right
                    }
                    2 -> {
                        this.current = 1
                        this.from = back
                    }
                    else -> throw IllegalArgumentException()
                }
                this.step = 0
            }
            return this.from.lerp(tip, this.step++ * this.delta)
        }
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