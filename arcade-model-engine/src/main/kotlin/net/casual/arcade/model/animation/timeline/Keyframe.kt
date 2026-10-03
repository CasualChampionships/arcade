/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.model.animation.timeline

import net.casual.arcade.model.animation.molang.MolangVec3
import net.casual.arcade.utils.math.Easing
import org.joml.Vector3f
import org.joml.Vector3fc

public class Keyframe(
    public val time: Float,
    public val pre: MolangVec3,
    public val post: MolangVec3 = pre,
    public val interpolation: KeyframeInterpolation,
    public val easing: Easing,
    public val bezier: BezierHandles? = null
) {
    public val isConstant: Boolean
        get() = this.pre.isConstant && this.post.isConstant

    public class BezierHandles(
        leftTime: Vector3fc,
        leftValue: Vector3fc,
        rightTime: Vector3fc,
        rightValue: Vector3fc
    ) {
        public val leftTime: Vector3fc = Vector3f(leftTime)
        public val leftValue: Vector3fc = Vector3f(leftValue)
        public val rightTime: Vector3fc = Vector3f(rightTime)
        public val rightValue: Vector3fc = Vector3f(rightValue)
    }
}