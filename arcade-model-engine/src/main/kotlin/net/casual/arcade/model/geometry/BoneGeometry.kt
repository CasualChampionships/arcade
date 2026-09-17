/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.model.geometry

import net.minecraft.resources.Identifier
import org.joml.Vector3f
import org.joml.Vector3fc

public class BoneGeometry private constructor(
    public val cubes: List<Cube>,
    public val model: Identifier,
    public val scale: Float,
    public val offset: Vector3fc
) {
    public companion object {
        public const val MAX_EXTENT: Float = 32.0F
        public const val MIN_EXTENT: Float = -16.0F
        public const val HALF_EXTENT: Float = 24.0F
        public val CENTER: Vector3fc = Vector3f(8.0F)

        public fun create(cubes: List<Cube>, model: Identifier, scale: Float, offset: Vector3fc): BoneGeometry {
            return BoneGeometry(cubes.toList(), model, scale, Vector3f(offset))
        }
    }
}