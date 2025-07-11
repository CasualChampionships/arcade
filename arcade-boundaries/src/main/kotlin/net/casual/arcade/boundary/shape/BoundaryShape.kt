/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.boundary.shape

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import net.casual.arcade.boundary.utils.BoundaryRegistries
import net.casual.arcade.utils.MathUtils.forEachCorner
import net.casual.arcade.utils.MathUtils.toAABB
import net.casual.arcade.utils.codec.CodecProvider.Companion.register
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.casual.arcade.visuals.shapes.ShapePoints
import net.minecraft.core.BlockPos
import net.minecraft.core.Registry
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import java.util.function.Function

/**
 * Represents the shape of a level boundary.
 */
public interface BoundaryShape {
    /**
     * This gets the approximate size of the boundary,
     * i.e. the x-length, y-length, and z-length.
     *
     * @return The size of the boundary.
     */
    public fun size(): Vec3

    /**
     * This gets the center of the boundary.
     *
     * @return The center of the boundary.
     */
    public fun center(): Vec3

    /**
     * Updates the shape of the border.
     */
    public fun tick()

    /**
     * Resizes the boundary to the specified [size] over
     * a [duration].
     *
     * A duration of [MinecraftTimeDuration.ZERO] makes
     * the resize instant.
     *
     * @param size The desired size.
     * @param duration The duration to change the size over.
     */
    public fun resize(size: Vec3, duration: MinecraftTimeDuration = MinecraftTimeDuration.ZERO)

    /**
     * Re-centers the boundary to the specified [center] over
     * a [duration].
     *
     * A duration of [MinecraftTimeDuration.ZERO] makes
     * the re-centering instant.
     *
     * @param center The desired center.
     * @param duration The duration to change the center over.
     */
    public fun recenter(center: Vec3, duration: MinecraftTimeDuration = MinecraftTimeDuration.ZERO)

    /**
     * Whether a given [BlockPos] is contained in the border.
     *
     * This treats the position as a unit cube,
     * see [contains] for implementation details.
     *
     * @param pos The pos to check.
     * @return The [Containment] of the position.
     */
    public fun contains(pos: BlockPos): Containment {
        return this.contains(pos.toAABB())
    }

    /**
     * Whether a given [box] is contained in the world border.
     *
     * This returns [Containment] which dictates whether the box
     * is fully, partially, or not contained within the border.
     *
     * @param box The box to check.
     * @return The [Containment] of the box.
     */
    public fun contains(box: AABB): Containment {
        var anyInside = false
        var anyOutside = false
        box.forEachCorner { x, y, z ->
            val contains = this.contains(x, y, z)
            if (contains) anyInside = true else anyOutside = true
            if (anyInside && anyOutside) return Containment.Partial
        }
        return if (anyInside) Containment.Full else Containment.None
    }

    /**
     * Checks whether a point given by [x], [y], [z]
     * is within the border.
     *
     * @param x The x coordinate of the point.
     * @param y The y coordinate of the point.
     * @param z The z coordinate of the point.
     * @return Whether the point is in the border.
     */
    public fun contains(x: Double, y: Double, z: Double): Boolean

    /**
     * Checks whether a [point] is within the border.
     *
     * @param point The point to check.
     * @return Whether the point is in the border.
     */
    public fun contains(point: Vec3): Boolean {
        return this.contains(point.x, point.y, point.z)
    }

    /**
     * Gets the distance between this boundary and a given [point].
     *
     * @param point The point to get the distance to.
     * @return The distance.
     */
    public fun getDistanceBetween(point: Vec3): Double {
        return this.getDirectionFrom(point).length()
    }

    /**
     * Gets the direction (with magnitude) from a given [point].
     *
     * @param point The point to get the direction to.
     */
    public fun getDirectionFrom(point: Vec3): Vec3

    /**
     * Gets the status of the border, whether
     * it's currently stationary, shrinking, or growing.
     *
     * @return The border status.
     */
    public fun getStatus(): Status

    /**
     * Gets the points that make up the world
     * border shape.
     *
     * @return Points on the world border.
     */
    public fun getPoints(): ShapePoints

    public fun codec(): MapCodec<out BoundaryShape>

    public enum class Containment {
        None, Partial, Full
    }

    public enum class Status {
        Stationary, Shrinking, Growing;

        public fun <T> choose(stationary: T, shrinking: T, growing: T): T {
            return when (this) {
                Stationary -> stationary
                Shrinking -> shrinking
                Growing -> growing
            }
        }

        public fun isStationary(): Boolean {
            return this == Stationary
        }

        public fun isMoving(): Boolean {
            return !this.isStationary()
        }
    }

    public companion object {
        public val CODEC: Codec<BoundaryShape> = Codec.lazyInitialized {
            BoundaryRegistries.BOUNDARY_SHAPE.byNameCodec()
                .dispatch(BoundaryShape::codec, Function.identity())
        }

        internal fun bootstrap(registry: Registry<MapCodec<out BoundaryShape>>) {
            AxisAlignedBoundaryShape.register(registry)
        }
    }
}