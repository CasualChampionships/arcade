/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils

import net.minecraft.core.Direction8
import net.minecraft.util.Mth
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import org.joml.Quaternionf
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.ceil

public object MathUtils {
    public operator fun Vec3.component1(): Double {
        return this.x
    }

    public operator fun Vec3.component2(): Double {
        return this.y
    }

    public operator fun Vec3.component3(): Double {
        return this.z
    }

    public operator fun Vec2.component1(): Float {
        return this.y
    }

    public operator fun Vec2.component2(): Float {
        return this.x
    }

    public fun Vec3.projectOntoLine(start: Vec3, end: Vec3): Vec3 {
        if (start == end) {
            return start
        }

        val direction = end.subtract(start)
        val vec = this.subtract(start)
        val projection = vec.dot(direction) / direction.lengthSqr()
        return start.add(direction.scale(projection))
    }

    public fun Vec3.projectionScalar(start: Vec3, end: Vec3): Double {
        if (start == end) {
            return 0.0
        }

        val direction = end.subtract(start)
        val vec = this.subtract(start)
        return vec.dot(direction) / direction.lengthSqr()
    }

    /**
     * Calculates the shortest distance to an infinite line given by the [start] and [direction].
     *
     * If you want the shortest distance to a finite line see [distanceToSegment].
     *
     * @param start The starting point of the line.
     * @param direction The direction of the line.
     * @return The shortest distance from `this` to the line.
     * @see distanceToSegment
     */
    public fun Vec3.distanceToLine(start: Vec3, direction: Vec3): Double {
        if (direction == Vec3.ZERO) {
            return this.distanceTo(start)
        }

        val cross = this.subtract(start).cross(direction)
        return cross.length() / direction.length()
    }

    /**
     * Calculates the shortest distance to a finite line given by the [start] and [end].
     *
     * @param start The start of the line.
     * @param end The end of the line.
     * @return The shortest distance from `this` to the line.
     */
    public fun Vec3.distanceToSegment(start: Vec3, end: Vec3): Double {
        if (start == end) {
            return this.distanceTo(start)
        }

        val scalar = this.projectionScalar(start, end)
        return when {
            scalar < 0.0 -> this.distanceTo(start)
            scalar > 1.0 -> this.distanceTo(end)
            else -> this.distanceToLine(start, end.subtract(start))
        }
    }

    public fun Vec3.rotationAnglesTowards(other: Vec3): Vec2 {
        val direction = other.subtract(this)
        val horizontal = direction.horizontalDistance()
        val xRot = Mth.wrapDegrees(-Mth.atan2(direction.y, horizontal).toFloat() * Mth.RAD_TO_DEG)
        val yRot = Mth.wrapDegrees(Mth.atan2(direction.z, direction.x).toFloat() * Mth.RAD_TO_DEG - 90.0F)
        return Vec2(xRot, yRot)
    }

    public fun Vec3.rotationTowards(other: Vec3): Quaternionf {
        // This stupid math took me like 8 hours to figure out.
        // I hate Quaternions - Sensei
        val normalized = other.subtract(this).normalize()
        val projected = Vec3(normalized.x, 0.0, normalized.z).normalize()

        val yaw = atan2(projected.x, projected.z).toFloat()
        val pitch = asin(-normalized.y).toFloat()

        return Quaternionf().rotateY(yaw).mul(Quaternionf().rotateX(pitch))
    }

    public fun Direction8.opposite(): Direction8 {
        return when (this) {
            Direction8.NORTH -> Direction8.SOUTH
            Direction8.NORTH_EAST -> Direction8.SOUTH_WEST
            Direction8.EAST -> Direction8.WEST
            Direction8.SOUTH_EAST -> Direction8.NORTH_WEST
            Direction8.SOUTH -> Direction8.NORTH
            Direction8.SOUTH_WEST -> Direction8.NORTH_EAST
            Direction8.WEST -> Direction8.EAST
            Direction8.NORTH_WEST -> Direction8.SOUTH_EAST
        }
    }

    public fun Double.wholeOrNull(): Int? {
        if (ceil(this) == this) {
            return this.toInt()
        }
        return null
    }

    public fun centeredScale(percent: Float, factor: Float): Float {
        val shift = (1 - factor) / 2.0F
        return shift + percent * factor
    }
}