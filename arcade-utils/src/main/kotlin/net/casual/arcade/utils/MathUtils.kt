/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils

import net.casual.arcade.utils.MathUtils.maxYCenter
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.Direction8
import net.minecraft.core.Vec3i
import net.minecraft.util.Mth
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import org.joml.Quaternionf
import kotlin.math.*

public object MathUtils {
    public operator fun Vec3.component1(): Double = this.x
    public operator fun Vec3.component2(): Double = this.y
    public operator fun Vec3.component3(): Double = this.z

    public operator fun Vec2.component1(): Float = this.y
    public operator fun Vec2.component2(): Float = this.x

    public operator fun Vec3i.component1(): Int = this.x
    public operator fun Vec3i.component2(): Int = this.y
    public operator fun Vec3i.component3(): Int = this.z

    public fun Vec3.verticalDistanceTo(other: Vec3): Double {
        return abs(this.y - other.y)
    }

    public fun Vec3.horizontalDistanceTo(other: Vec3): Double {
        return sqrt(this.horizontalDistanceToSqr(other))
    }

    public fun Vec3.horizontalDistanceToSqr(other: Vec3): Double {
        val dx = this.x - other.x
        val dz = this.z - other.z
        return dx * dx + dz * dz
    }

    public fun Vec3.verticallyCloserThan(other: Vec3, distance: Double): Boolean {
        return this.verticalDistanceTo(other) < distance
    }

    public fun Vec3.horizontallyCloserThan(other: Vec3, distance: Double): Boolean {
        return this.horizontalDistanceToSqr(other) < distance * distance
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

    public fun BlockPos.toAABB(): AABB {
        val (x, y, z) = this
        return AABB(x + 0.0, y + 0.0, z + 0.0, x + 1.0, y + 1.0, z + 1.0)
    }

    public inline fun Vec3i.forEachCorner(consumer: (Int, Int, Int) -> Unit) {
        consumer.invoke(this.x, this.y, this.z)
        consumer.invoke(this.x, this.y, this.z + 1)
        consumer.invoke(this.x, this.y + 1, this.z)
        consumer.invoke(this.x, this.y + 1, this.z + 1)
        consumer.invoke(this.x + 1, this.y, this.z)
        consumer.invoke(this.x + 1, this.y, this.z + 1)
        consumer.invoke(this.x + 1, this.y + 1, this.z)
        consumer.invoke(this.x + 1, this.y + 1, this.z + 1)
    }

    public inline fun AABB.forEachCorner(consumer: (Double, Double, Double) -> Unit) {
        consumer.invoke(this.minX, this.minY, this.minZ)
        consumer.invoke(this.minX, this.minY, this.maxZ)
        consumer.invoke(this.minX, this.maxY, this.minZ)
        consumer.invoke(this.minX, this.maxY, this.maxZ)
        consumer.invoke(this.maxX, this.minY, this.minZ)
        consumer.invoke(this.maxX, this.minY, this.maxZ)
        consumer.invoke(this.maxX, this.maxY, this.minZ)
        consumer.invoke(this.maxX, this.maxY, this.maxZ)
    }

    public val AABB.maxXCenter: Vec3
        get() = Vec3(this.maxX, Mth.lerp(0.5, this.minY, this.maxY), Mth.lerp(0.5, this.minZ, this.maxZ))

    public val AABB.minXCenter: Vec3
        get() = Vec3(this.minX, Mth.lerp(0.5, this.minY, this.maxY), Mth.lerp(0.5, this.minZ, this.maxZ))

    public val AABB.maxYCenter: Vec3
        get() = Vec3(Mth.lerp(0.5, this.minX, this.maxX), this.maxY, Mth.lerp(0.5, this.minZ, this.maxZ))

    public val AABB.minYCenter: Vec3
        get() = this.bottomCenter

    public val AABB.maxZCenter: Vec3
        get() = Vec3(Mth.lerp(0.5, this.minX, this.maxX), Mth.lerp(0.5, this.minY, this.maxY), this.maxZ)

    public val AABB.minZCenter: Vec3
        get() = Vec3(Mth.lerp(0.5, this.minX, this.maxX), Mth.lerp(0.5, this.minY, this.maxY), this.minZ)

    /**
     * Gets the center point for a given [face] indicated
     * by which direction the face is facing (outwards).
     *
     * @param face The face.
     * @return The central position of that face.
     */
    public fun AABB.getFaceCenter(face: Direction): Vec3 {
        return when (face) {
            Direction.EAST -> this.maxXCenter
            Direction.WEST -> this.minXCenter
            Direction.UP -> this.maxYCenter
            Direction.DOWN -> this.minYCenter
            Direction.SOUTH -> this.maxZCenter
            Direction.NORTH -> this.minZCenter
        }
    }

    public fun AABB.getFace(face: Direction): Double {
        return when (face) {
            Direction.EAST -> this.maxX
            Direction.WEST -> this.minX
            Direction.UP -> this.maxY
            Direction.DOWN -> this.minY
            Direction.SOUTH -> this.maxZ
            Direction.NORTH -> this.minZ
        }
    }

    public fun above(y: Double): AABB {
        return AABB(
            Double.NEGATIVE_INFINITY, 
            y, 
            Double.NEGATIVE_INFINITY, 
            Double.POSITIVE_INFINITY, 
            Double.POSITIVE_INFINITY, 
            Double.POSITIVE_INFINITY
        )
    }

    public fun AABB.getSizeVec(): Vec3 {
        return Vec3(this.xsize, this.ysize, this.zsize)
    }
}