/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.utils

import net.minecraft.core.Direction.Axis
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.joml.Vector3d

public class AABBShapeIterator(
    private val min: Vec3,
    private val max: Vec3,
    private val pointsPerUnit: Double
): Iterator<Vec3> {
    private val dx = this.max.x - this.min.x
    private val dy = this.max.y - this.min.y
    private val dz = this.max.z - this.min.z

    private val xSteps = (this.dx * this.pointsPerUnit).toInt().coerceAtLeast(1)
    private val ySteps = (this.dy * this.pointsPerUnit).toInt().coerceAtLeast(1)
    private val zSteps = (this.dz * this.pointsPerUnit).toInt().coerceAtLeast(1)

    private var face = 0
    private var i = 0
    private var j = 0

    private var fixed = 0.0
    private var iMax = 0
    private var jMax = 0
    private var axisX = Axis.X
    private var axisY = Axis.Y

    private val result = Vector3d(0.0, 0.0, 0.0)

    public constructor(aabb: AABB, pointsPerUnit: Double): this(aabb.minPosition, aabb.maxPosition, pointsPerUnit)

    init {
        setupFace()
    }

    override fun hasNext(): Boolean {
        return this.face < 6
    }

    override fun next(): Vec3 {
        val t = this.i.toDouble() / this.iMax
        val u = this.j.toDouble() / this.jMax

        this.result.x = this.min.x +
            (if (this.axisX == Axis.X) this.dx * t else if (this.axisY == Axis.X) this.dx * u else 0.0)
        this.result.y = this.min.y +
            (if (this.axisX == Axis.Y) this.dy * t else if (this.axisY == Axis.Y) this.dy * u else 0.0)
        this.result.z = this.min.z +
            (if (this.axisX == Axis.Z) this.dz * t else if (this.axisY == Axis.Z) this.dz * u else 0.0)

        when (this.face) {
            0, 1 -> this.result.x = this.fixed  // -X or +X
            2, 3 -> this.result.y = this.fixed  // -Y or +Y
            4, 5 -> this.result.z = this.fixed  // -Z or +Z
        }

        if (++this.j > this.jMax) {
            this.j = 0
            if (++this.i > this.iMax) {
                this.i = 0
                this.face++
                if (this.hasNext()) {
                    this.setupFace()
                }
            }
        }

        return Vec3(this.result.x, this.result.y, this.result.z)
    }

    private fun setupFace() {
        when (this.face) {
            0 -> { // -X
                this.fixed = this.min.x
                this.iMax = this.ySteps
                this.jMax = this.zSteps
                this.axisX = Axis.Y
                this.axisY = Axis.Z
            }
            1 -> { // +X
                this.fixed = this.max.x
                this.iMax = this.ySteps
                this.jMax = this.zSteps
                this.axisX = Axis.Y
                this.axisY = Axis.Z
            }
            2 -> { // -Y
                this.fixed = this.min.y
                this.iMax = this.xSteps
                this.jMax = this.zSteps
                this.axisX = Axis.X
                this.axisY = Axis.Z
            }
            3 -> { // +Y
                this.fixed = this.max.y
                this.iMax = this.xSteps
                this.jMax = this.zSteps
                this.axisX = Axis.X
                this.axisY = Axis.Z
            }
            4 -> { // -Z
                this.fixed = this.min.z
                this.iMax = this.xSteps
                this.jMax = this.ySteps
                this.axisX = Axis.X
                this.axisY = Axis.Y
            }
            else -> { // +Z
                this.fixed = this.max.z
                this.iMax = this.xSteps
                this.jMax = this.ySteps
                this.axisX = Axis.X
                this.axisY = Axis.Y
            }
        }
    }
}