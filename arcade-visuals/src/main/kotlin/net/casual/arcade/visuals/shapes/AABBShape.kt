/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.shapes

import net.casual.arcade.visuals.utils.AABBShapeIterator
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3

public class AABBShape(
    public val aabb: AABB
): ShapePoints {
    override fun iterator(steps: Int): Iterator<Vec3> {
        return AABBShapeIterator(this.aabb, steps / 10.0)
    }
}