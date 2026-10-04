/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.model.geometry

import net.minecraft.core.Direction
import org.joml.Vector3f
import org.joml.Vector3fc

public class Cube private constructor(
    public val from: Vector3fc,
    public val to: Vector3fc,
    public val origin: Vector3fc,
    public val rotation: Vector3fc,
    public val faces: Map<Direction, CubeFace>,
    public val shade: Boolean = true,
    public val lightEmission: Int = 0
) {
    public val rotated: Boolean
        get() = this.rotation.x() != 0.0F || this.rotation.y() != 0.0F || this.rotation.z() != 0.0F

    public fun translate(shift: Vector3fc): Cube {
        return Cube(
            this.from.add(shift, Vector3f()),
            this.to.add(shift, Vector3f()),
            this.origin.add(shift, Vector3f()),
            this.rotation, this.faces, this.shade, this.lightEmission
        )
    }

    public fun scale(scale: Float): Cube {
        return Cube(
            this.from.mul(scale, Vector3f()),
            this.to.mul(scale, Vector3f()),
            this.origin.mul(scale, Vector3f()),
            this.rotation, this.faces, this.shade, this.lightEmission
        )
    }

    public companion object {
        public fun create(
            from: Vector3fc,
            to: Vector3fc,
            origin: Vector3fc,
            rotation: Vector3fc,
            faces: Map<Direction, CubeFace>,
            shade: Boolean = true,
            lightEmission: Int = 0
        ): Cube {
            return Cube(
                Vector3f(from), Vector3f(to), Vector3f(origin), Vector3f(rotation), faces.toMap(), shade, lightEmission
            )
        }
    }
}