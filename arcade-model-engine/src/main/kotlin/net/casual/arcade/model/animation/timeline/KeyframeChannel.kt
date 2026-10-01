/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.model.animation.timeline

import net.casual.arcade.model.animation.molang.MolangScope
import net.casual.arcade.utils.MathUtils
import net.casual.arcade.utils.math.Easing
import net.minecraft.util.Mth
import org.joml.Math
import org.joml.Vector3f
import kotlin.math.abs

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

        val afterIndex = keyframes.indexOfFirst { it.time > time }
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
            KeyframeInterpolation.Step -> dest.set(start)
            KeyframeInterpolation.CatmullRom -> {
                val p0 = keyframes.getOrNull(beforeIndex - 1)?.pre?.eval(scope) ?: start
                val p3 = keyframes.getOrNull(afterIndex + 1)?.pre?.eval(scope) ?: end
                this.catmullRom(p0, start, end, p3, alpha, dest)
            }
            KeyframeInterpolation.Bezier -> {
                val right = before.bezier
                val left = after.bezier
                if (right == null || left == null) {
                    start.lerp(end, alpha, dest)
                } else {
                    this.bezier(start, end, right, left, duration, alpha, dest)
                }
            }
        }
    }

    private fun catmullRom(p0: Vector3f, p1: Vector3f, p2: Vector3f, p3: Vector3f, t: Float, dest: Vector3f): Vector3f {
        return dest.set(
            MathUtils.catmullRom(p0.x, p1.x, p2.x, p3.x, t),
            MathUtils.catmullRom(p0.y, p1.y, p2.y, p3.y, t),
            MathUtils.catmullRom(p0.z, p1.z, p2.z, p3.z, t)
        )
    }

    private fun bezier(
        start: Vector3f,
        end: Vector3f,
        right: Keyframe.BezierHandles,
        left: Keyframe.BezierHandles,
        duration: Float,
        alpha: Float,
        dest: Vector3f
    ): Vector3f {
        return dest.set(
            this.bezier(start.x, end.x, right.rightTime.x() / duration, right.rightValue.x(), 1 + left.leftTime.x() / duration, left.leftValue.x(), alpha),
            this.bezier(start.y, end.y, right.rightTime.y() / duration, right.rightValue.y(), 1 + left.leftTime.y() / duration, left.leftValue.y(), alpha),
            this.bezier(start.z, end.z, right.rightTime.z() / duration, right.rightValue.z(), 1 + left.leftTime.z() / duration, left.leftValue.z(), alpha),
        )
    }

    private fun bezier(start: Float, end: Float, h1: Float, v1: Float, h2: Float, v2: Float, alpha: Float): Float {
        val t = this.solveBezier(alpha, h1, h2)
        return this.cubicBezier(start, start + v1, end + v2, end, t)
    }

    private fun solveBezier(time: Float, h1: Float, h2: Float): Float {
        var t = 0.5F
        repeat(20) {
            val error = this.cubicBezier(0.0F, h1, h2, 1.0F, t) - time
            if (abs(error) < Mth.EPSILON) {
                return t
            }
            val derivative = this.bezierDerivative(h1, h2, t)
            if (derivative != 0.0F) {
                t -= error / derivative
            }
            t = Math.clamp(0.0F, 1.0F, t)
        }
        return t
    }

    private fun cubicBezier(p0: Float, p1: Float, p2: Float, p3: Float, t: Float): Float {
        val u = 1.0F - t
        return u * u * u * p0 + 3.0F * u * u * t * p1 + 3.0F * u * t * t * p2 + t * t * t * p3
    }

    private fun bezierDerivative(p1: Float, p2: Float, t: Float): Float {
        val u = 1.0F - t
        return 3.0F * u * u * p1 + 6.0F * u * t * (p2 - p1) + 3.0F * t * t * (1 - p2)
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