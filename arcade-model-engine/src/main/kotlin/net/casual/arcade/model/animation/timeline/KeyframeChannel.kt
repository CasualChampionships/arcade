/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.model.animation.timeline

import net.casual.arcade.model.animation.molang.MolangScope
import net.casual.arcade.utils.math.Easing
import org.joml.Vector3f

public class KeyframeChannel private constructor(
    private val keyframes: List<Keyframe>
) {
    public val isConstant: Boolean = this.keyframes.all { keyframe -> keyframe.isConstant }

    public val length: Float
        get() = if (this.keyframes.isEmpty()) 0.0F else this.keyframes.last().time

    public fun sample(time: Float, scope: MolangScope, dest: Vector3f = Vector3f()): Vector3f {
        val keyframes = this.keyframes
        if (keyframes.isEmpty()) {
            return dest.zero()
        }

        val afterIndex = keyframes.binarySearch { keyframe -> keyframe.time.compareTo(time) }
        if (afterIndex == 0) {
            return keyframes.first().pre.eval(scope, dest)
        }
        if (afterIndex == -1) {
            return keyframes.last().post.eval(scope, dest)
        }
        val beforeIndex = afterIndex - 1
        val before = keyframes[beforeIndex]
        val after = keyframes[afterIndex]

        val duration = after.time - before.time
        var alpha = if (duration <= 0.0F) 1.0F else (time - before.time) / duration
        if (after.easing !== Easing.LINEAR) {
            alpha = after.easing.apply(alpha)
        }

        val start = before.post.eval(scope)
        val end = after.pre.eval(scope)
        return when (after.interpolation) {
            KeyframeInterpolation.Linear -> start.lerp(end, alpha, dest)
            else -> TODO()
        }
    }

    public enum class Type {
        Position, Rotation, Scale;
    }

    public companion object {
        public fun create(keyframes: List<Keyframe>): KeyframeChannel {
            return KeyframeChannel(keyframes.sortedBy { keyframe -> keyframe.time })
        }
    }
}