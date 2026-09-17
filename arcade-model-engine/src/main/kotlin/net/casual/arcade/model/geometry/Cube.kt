/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.model.geometry

import net.minecraft.core.Direction
import org.joml.Vector3fc

public class Cube(
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
}