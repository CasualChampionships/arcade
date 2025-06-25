/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.border.shape

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.border.shape.BoundaryShape.Status
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.BoundingBoxUtils.getSizeVec
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.codec.CodecProvider
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.casual.arcade.visuals.utils.AABBShapeIterator
import net.minecraft.core.Direction.Axis
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.ExtraCodecs
import net.minecraft.util.Mth
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import java.util.function.Function

public class AxisAlignedBoundaryShape private constructor(
    private var size: State,
    private var center: State,
    private val pointsPerBlock: Double
): BoundaryShape {
    private var aabb: AABB = this.recalculateAABB()

    public constructor(
        aabb: AABB, pointsPerBlock: Double
    ): this(State.Static(aabb.getSizeVec()), State.Static(aabb.center), pointsPerBlock)

    override fun size(): Vec3 {
        return this.size.current
    }

    override fun center(): Vec3 {
        return this.center.current
    }

    override fun tick() {
        val dirty = this.size is State.Moving || this.center is State.Moving
        this.size = this.size.tick()
        this.center = this.center.tick()

        if (dirty) {
            this.aabb = this.recalculateAABB()
        }
    }

    override fun resize(size: Vec3, duration: MinecraftTimeDuration) {
        if (duration.isZero) {
            this.size = State.Static(size)
            this.aabb = this.recalculateAABB()
            return
        }
        this.size = State.Moving(this.size.current, size, duration)
    }

    override fun recenter(center: Vec3, duration: MinecraftTimeDuration) {
        if (duration.isZero) {
            this.center = State.Static(center)
            this.aabb = this.recalculateAABB()
            return
        }
        this.center = State.Moving(this.center.current, center, duration)
    }

    override fun contains(x: Double, y: Double, z: Double): Boolean {
        return this.aabb.contains(x, y, z)
    }

    override fun contains(point: Vec3): Boolean {
        return this.aabb.contains(point)
    }

    override fun getDirectionTo(point: Vec3): Vec3 {
        val min = this.aabb.minPosition
        val max = this.aabb.maxPosition
        if (!this.contains(point)) {
            val clampedX = point.x.coerceIn(min.x, max.x)
            val clampedY = point.y.coerceIn(min.y, max.y)
            val clampedZ = point.z.coerceIn(min.z, max.z)
            return Vec3(clampedX, clampedY, clampedZ).subtract(point)
        }

        val distToMinX = point.x - min.x
        val distToMaxX = max.x - point.x
        val distToMinY = point.y - min.y
        val distToMaxY = max.y - point.y
        val distToMinZ = point.z - min.z
        val distToMaxZ = max.z - point.z

        val xDir = if (distToMinX < distToMaxX) Vec3(-distToMinX, 0.0, 0.0) else Vec3(distToMaxX, 0.0, 0.0)
        val yDir = if (distToMinY < distToMaxY) Vec3(0.0, -distToMinY, 0.0) else Vec3(0.0, distToMaxY, 0.0)
        val zDir = if (distToMinZ < distToMaxZ) Vec3(0.0, 0.0, -distToMinZ) else Vec3(0.0, 0.0, distToMaxZ)
        return listOf(xDir, yDir, zDir).minBy { it.lengthSqr() }
    }

    override fun getStatus(): Status {
        return this.size.status()
    }

    override fun getFaceCount(): Int {
        return 6
    }

    override fun getFaces(): List<BoundaryShape.Face> {
        val min = this.aabb.minPosition
        val max = this.aabb.maxPosition
        // +X, -X, +Y, -Y, +Z, -Z
        return listOf(
            BoundaryShape.Face(Vec3(max.x, min.y, min.z), Vec3(max.x, max.y, min.z), Vec3(max.x, max.y, max.z), Vec3(max.x, min.y, max.z)),
            BoundaryShape.Face(Vec3(min.x, min.y, max.z), Vec3(min.x, max.y, max.z), Vec3(min.x, max.y, min.z), Vec3(min.x, min.y, min.z)),
            BoundaryShape.Face(Vec3(min.x, max.y, min.z), Vec3(min.x, max.y, max.z), Vec3(max.x, max.y, max.z), Vec3(max.x, max.y, min.z)),
            BoundaryShape.Face(Vec3(min.x, min.y, max.z), Vec3(min.x, min.y, min.z), Vec3(max.x, min.y, min.z), Vec3(max.x, min.y, max.z)),
            BoundaryShape.Face(Vec3(min.x, min.y, max.z), Vec3(max.x, min.y, max.z), Vec3(max.x, max.y, max.z), Vec3(min.x, max.y, max.z)),
            BoundaryShape.Face(Vec3(max.x, min.y, min.z), Vec3(min.x, min.y, min.z), Vec3(min.x, max.y, min.z), Vec3(max.x, max.y, min.z)),
        )
    }

    override fun getPoints(): Iterable<Vec3> {
        return Iterable { AABBShapeIterator(this.aabb, this.pointsPerBlock) }
    }

    override fun codec(): MapCodec<out BoundaryShape> {
        return CODEC
    }

    private fun recalculateAABB(): AABB {
        return AABB.ofSize(this.center.current, this.size.current.x, this.size.current.y, this.size.current.z)
    }

    private fun sampleFace(min: Vec3, max: Vec3, xs: Int, ys: Int, axis: Axis, fixed: Double, xAxis: Axis, yAxis: Axis): Sequence<Vec3> = sequence {
        for (i in 0..xs) {
            val a = i.toDouble() / xs
            for (j in 0..ys) {
                val b = j.toDouble() / ys

                val coords = DoubleArray(3)
                coords[axis.ordinal] = fixed
                coords[xAxis.ordinal] = Mth.lerp(a, min.get(xAxis), max.get(xAxis))
                coords[yAxis.ordinal] = Mth.lerp(b, min.get(yAxis), max.get(yAxis))

                yield(Vec3(coords[0], coords[1], coords[2]))
            }
        }
    }

    public companion object: CodecProvider<AxisAlignedBoundaryShape> {
        override val ID: ResourceLocation = ArcadeUtils.id("axis_aligned_border_shape")
        override val CODEC: MapCodec<AxisAlignedBoundaryShape> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                State.CODEC.fieldOf("size_state").forGetter(AxisAlignedBoundaryShape::size),
                State.CODEC.fieldOf("center_state").forGetter(AxisAlignedBoundaryShape::center),
                Codec.DOUBLE.fieldOf("points_per_block").forGetter(AxisAlignedBoundaryShape::pointsPerBlock)
            ).apply(instance, ::AxisAlignedBoundaryShape)
        }
    }

    private sealed interface State {
        val start: Vec3
        val current: Vec3
        val end: Vec3
        val duration: MinecraftTimeDuration

        fun tick(): State

        fun status(): Status

        fun codec(): MapCodec<out State>

        data class Static(override val current: Vec3): State {
            override val start: Vec3 get() = this.current
            override val end: Vec3 get() = this.current
            override val duration: MinecraftTimeDuration = MinecraftTimeDuration.ZERO

            override fun tick(): State {
                return this
            }

            override fun status(): Status {
                return Status.Stationary
            }

            override fun codec(): MapCodec<out State> {
                return CODEC
            }

            companion object {
                val CODEC: MapCodec<Static> = RecordCodecBuilder.mapCodec { instance ->
                    instance.group(
                        Vec3.CODEC.fieldOf("current").forGetter(Static::current)
                    ).apply(instance, ::Static)
                }
            }
        }

        class Moving(
            override val start: Vec3,
            override val end: Vec3,
            override val duration: MinecraftTimeDuration,
            private var elapsed: MinecraftTimeDuration = 0.Ticks
        ): State {
            override var current: Vec3 = this.start

            override fun tick(): State {
                this.elapsed += 1.Ticks
                if (this.elapsed >= this.duration) {
                    return Static(this.end)
                }

                val delta = this.elapsed / this.duration
                this.current = this.start.lerp(this.end, delta)
                return this
            }

            override fun status(): Status {
                return if (this.end.lengthSqr() > this.start.lengthSqr()) Status.Growing else Status.Shrinking
            }

            override fun codec(): MapCodec<out State> {
                return CODEC
            }

            companion object {
                val CODEC: MapCodec<Moving> = RecordCodecBuilder.mapCodec { instance ->
                    instance.group(
                        Vec3.CODEC.fieldOf("start").forGetter(Moving::start),
                        Vec3.CODEC.fieldOf("end").forGetter(Moving::end),
                        MinecraftTimeDuration.CODEC.fieldOf("duration").forGetter(Moving::duration),
                        MinecraftTimeDuration.CODEC.fieldOf("elapsed").forGetter(Moving::elapsed)
                    ).apply(instance, ::Moving)
                }
            }
        }

        companion object {
            private val MAPPER = ExtraCodecs.LateBoundIdMapper<String, MapCodec<out State>>()
            val CODEC: Codec<State> = MAPPER.codec(Codec.STRING)
                .dispatch("type", State::codec, Function.identity())

            init {
                MAPPER.put("static", Static.CODEC)
                MAPPER.put("moving", Moving.CODEC)
            }
        }
    }


    private class AxisAlignedBorderIterator(
        private val min: Vec3,
        private val max: Vec3,
        pointsPerUnit: Double
    ) : Iterator<Vec3> {
        private val xSteps = ((this.max.x - this.min.x) * pointsPerUnit).toInt().coerceAtLeast(1)
        private val ySteps = ((this.max.y - this.min.y) * pointsPerUnit).toInt().coerceAtLeast(1)
        private val zSteps = ((this.max.z - this.min.z) * pointsPerUnit).toInt().coerceAtLeast(1)

        // 0 = -X, 1 = +X, 2 = -Y, 3 = +Y, 4 = -Z, 5 = +Z
        private var face = 0
        private var i = 0
        private var j = 0

        override fun hasNext(): Boolean = face < 6

        override fun next(): Vec3 {
            if (!hasNext()) throw NoSuchElementException()

            // determine fixed axis value and step counts for this face
            val (fixed, iMax, jMax, axisX, axisY) = when (face) {
                0 -> FaceConfig(this.min.x, ySteps, zSteps, Axis.Y, Axis.Z) // -X
                1 -> FaceConfig(this.max.x, ySteps, zSteps, Axis.Y, Axis.Z) // +X
                2 -> FaceConfig(this.min.y, xSteps, zSteps, Axis.X, Axis.Z) // -Y
                3 -> FaceConfig(this.max.y, xSteps, zSteps, Axis.X, Axis.Z) // +Y
                4 -> FaceConfig(this.min.z, xSteps, ySteps, Axis.X, Axis.Y) // -Z
                else -> FaceConfig(this.max.z, xSteps, ySteps, Axis.X, Axis.Y) // +Z
            }

            // compute t,u in [0,1]
            val t = i.toDouble() / iMax
            val u = j.toDouble() / jMax

            val coordX = this.min.x + (this.max.x - this.min.x) * (if (axisX == Axis.X) t else if (axisY == Axis.X) u else 0.0)
            val coordY = this.min.y + (this.max.y - this.min.y) * (if (axisX == Axis.Y) t else if (axisY == Axis.Y) u else 0.0)
            val coordZ = this.min.z + (this.max.z - this.min.z) * (if (axisX == Axis.Z) t else if (axisY == Axis.Z) u else 0.0)

            val result = when (this.face) {
                0, 1 -> Vec3(fixed, coordY, coordZ)
                2, 3 -> Vec3(coordX, fixed, coordZ)
                else  -> Vec3(coordX, coordY, fixed)
            }

            if (++this.j > jMax) {
                this.j = 0
                if (++this.i > iMax) {
                    this.i = 0
                    this.face++
                }
            }
            return result
        }

        private data class FaceConfig(
            val fixed: Double,
            val iMax: Int,
            val jMax: Int,
            val axisX: Axis,
            val axisY: Axis
        )
    }
}