/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.model.animation.timeline

import net.casual.arcade.model.animation.molang.MolangScope
import net.casual.arcade.model.animation.pose.BoneTransform

public class BoneTimeline(
    public val position: KeyframeChannel?,
    public val rotation: KeyframeChannel?,
    public val scale: KeyframeChannel?
) {
    public val isConstant: Boolean
        get() = this.position.isConstantOrNull && this.rotation.isConstantOrNull && this.scale.isConstantOrNull

    public val length: Float
        get() = maxOf(this.position?.length ?: 0.0F, this.rotation?.length ?: 0.0F, this.scale?.length ?: 0.0F)

    public fun sample(time: Float, scope: MolangScope, dest: BoneTransform): BoneTransform {
        dest.identity()
        this.position?.sample(time, scope, dest.position)
        this.rotation?.sample(time, scope, dest.rotation)
        this.scale?.sample(time, scope, dest.scale)
        return dest
    }

    private companion object {
        val KeyframeChannel?.isConstantOrNull
            get() = this == null || this.isConstant
    }
}