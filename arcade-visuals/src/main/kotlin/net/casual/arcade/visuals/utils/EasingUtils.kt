/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.utils

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

public object EasingUtils {
    public fun linear(x: Float): Float {
        return x
    }

    public fun easeInQuad(x: Float): Float {
        return x.square()
    }
    
    public fun easeOutQuad(x: Float): Float {
        return x * (2.0F - x)
    }
    
    public fun easeInOutQuad(x: Float): Float {
        return if (x < 0.5F) 2.0F * x.square() else -1.0F + (4.0F - 2.0F * x) * x
    }
    
    public fun easeInCubic(x: Float): Float {
        return x.cube()
    }

    public fun easeOutCubic(x: Float): Float {
        return 1.0F - (1.0F - x).cube()
    }

    public fun easeInOutCubic(x: Float): Float {
        return if (x < 0.5f) 4f * x.cube() else 1.0F - (-2.0F * x + 2.0F).cube() / 2.0F
    }
    
    public fun easeInSine(x: Float): Float {
        return 1.0F - cos((x * PI) / 2.0F).toFloat()
    }

    public fun easeOutSine(x: Float): Float {
        return sin((x * PI) / 2.0F).toFloat()
    }

    public fun easeInOutSine(x: Float): Float {
        return -(cos(PI.toFloat() * x) - 1.0F) / 2.0F
    }
    
    public fun easeInExpo(x: Float): Float {
        return if (x == 0.0f) 0.0f else 2.0.pow(10.0 * (x - 1.0)).toFloat()
    }

    public fun easeOutExpo(x: Float): Float {
        return if (x == 1.0F) 1.0F else 1.0F - 2.0.pow(-10.0 * x.toDouble()).toFloat()
    }

    public fun easeInCirc(x: Float): Float {
        return 1.0F - sqrt(1.0F - x.square())
    }
    public fun easeOutCirc(x: Float): Float {
        return sqrt(1.0F - (x - 1.0F).square())
    }
    
    public fun easeOutBack(x: Float): Float {
        val c1 = 1.70158F
        val c3 = c1 + 1.0F
        return 1.0F + c3 * (x - 1.0F).cube() + c1 * (x - 1.0F).square()
    }

    private fun Float.square(): Float {
        return this * this
    }

    private fun Float.cube(): Float {
        return this * this * this
    }
}