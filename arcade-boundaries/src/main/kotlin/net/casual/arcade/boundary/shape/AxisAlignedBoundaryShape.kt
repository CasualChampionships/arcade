/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.boundary.shape

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.boundary.renderer.AxisAlignedDisplayBoundaryRenderer
import net.casual.arcade.boundary.shape.BoundaryShape.Status
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.MathUtils.getSizeVec
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.codec.CodecProvider
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.casual.arcade.visuals.shapes.ShapePoints
import net.casual.arcade.visuals.shapes.impl.CuboidShape
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.ExtraCodecs
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import java.util.function.Function

/**
 * This is an implementation of [BoundaryShape] that
 * forms an axis aligned cuboid shape.
 *
 * @see AxisAlignedDisplayBoundaryRenderer
 */
public class AxisAlignedBoundaryShape private constructor(
    private var size: State,
    private var center: State
): BoundaryShape {
    /**
     * The axis aligned bounding box corresponding to the
     * shape of this boundary.
     */
    public var aabb: AABB = this.recalculateAABB()
        private set

    /**
     * Constructs this boundary shape with a given [AABB].
     *
     * @param aabb The bounding box for the shape.
     */
    public constructor(aabb: AABB): this(State.Static(aabb.getSizeVec()), State.Static(aabb.center))

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

    override fun getPoints(): ShapePoints {
        return CuboidShape(this.aabb)
    }

    override fun codec(): MapCodec<out BoundaryShape> {
        return CODEC
    }

    private fun recalculateAABB(): AABB {
        return AABB.ofSize(this.center.current, this.size.current.x, this.size.current.y, this.size.current.z)
    }

    public companion object: CodecProvider<AxisAlignedBoundaryShape> {
        override val ID: ResourceLocation = ArcadeUtils.id("axis_aligned_border_shape")
        override val CODEC: MapCodec<AxisAlignedBoundaryShape> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                State.CODEC.fieldOf("size_state").forGetter(AxisAlignedBoundaryShape::size),
                State.CODEC.fieldOf("center_state").forGetter(AxisAlignedBoundaryShape::center)
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
}