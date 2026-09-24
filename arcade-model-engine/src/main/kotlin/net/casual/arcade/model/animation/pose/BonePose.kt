/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.model.animation.pose

import org.joml.Matrix4f
import org.joml.Matrix4fc
import org.joml.Quaternionf
import org.joml.Vector3f

public class BonePose {
    private val matrix = Matrix4f()

    public fun matrix(): Matrix4fc {
        return this.matrix
    }

    public fun position(dest: Vector3f = Vector3f()): Vector3f {
        return this.matrix.getTranslation(dest)
    }

    public fun rotation(dest: Quaternionf = Quaternionf()): Quaternionf {
        return this.matrix.getUnnormalizedRotation(dest)
    }

    public fun scale(dest: Vector3f = Vector3f()): Vector3f {
        return this.matrix.getScale(dest)
    }

    internal fun raw(): Matrix4f {
        return this.matrix
    }
}
