/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.model.animation.pose

import org.joml.Vector3f

public class BoneTransform(
    public val position: Vector3f = Vector3f(),
    public val rotation: Vector3f = Vector3f(),
    public val scale: Vector3f = Vector3f(1.0F)
) {
    public fun set(other: BoneTransform): BoneTransform {
        this.position.set(other.position)
        this.rotation.set(other.rotation)
        this.scale.set(other.scale)
        return this
    }

    public fun identity(): BoneTransform {
        this.position.zero()
        this.rotation.zero()
        this.scale.set(1.0F)
        return this
    }

    public fun copy(): BoneTransform {
        return BoneTransform(Vector3f(this.position), Vector3f(this.rotation), Vector3f(this.scale))
    }
}
