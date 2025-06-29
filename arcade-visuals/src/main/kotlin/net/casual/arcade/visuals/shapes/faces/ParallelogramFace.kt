/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.shapes.faces

import net.minecraft.world.phys.Vec3

public class ParallelogramFace(
    public val origin: Vec3,
    public val edgeA: Vec3,
    public val edgeB: Vec3
): ShapeFace