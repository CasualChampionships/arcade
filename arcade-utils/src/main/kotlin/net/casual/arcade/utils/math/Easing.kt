/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.math

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

public fun interface Easing {
    public fun apply(t: Double): Double

    public fun apply(t: Float): Float {
        return this.apply(t.toDouble()).toFloat()
    }

    public fun out(): Easing {
        return Easing { t -> 1 - this.apply(1 - t) }
    }

    public fun inOut(): Easing {
        return Easing { t ->
            if (t < 0.5) this.apply(t * 2) / 2 else 1 - this.apply((1 - t) * 2) / 2
        }
    }

    public companion object {
        public val LINEAR: Easing = Easing { t -> t }
        public val QUAD: Easing = Easing { t -> t * t }
        public val CUBIC: Easing = Easing { t -> t * t * t }
        public val QUART: Easing = Easing { t -> t.pow(4) }
        public val QUINT: Easing = Easing { t -> t.pow(5) }
        public val SINE: Easing = Easing { t -> 1 - cos((t * PI) / 2) }
        public val CIRCLE: Easing = Easing { t -> 1 - sqrt(1 - t * t) }
        public val EXPO: Easing = Easing { t -> if (t == 0.0) 0.0 else 2.0.pow(10 * (t - 1)) }
        public val ELASTIC: Easing = elastic()
        public val BACK: Easing = back()
        public val BOUNCE: Easing = bounce()

        public fun poly(exponent: Double = 2.0): Easing {
            return Easing { t -> t.pow(exponent) }
        }

        public fun elastic(period: Double = 1.0): Easing {
            val p = period * PI
            return Easing { t -> 1 - cos((t * PI) / 2).pow(3) * cos(t * p) }
        }

        public fun back(overshoot: Double = 1.70158): Easing {
            return Easing { t -> t * t * ((overshoot + 1) * t - overshoot) }
        }

        public fun bounce(bounciness: Double = 0.5): Easing {
            @Suppress("UnnecessaryVariable")
            val k = bounciness
            return Easing { t ->
                val q = (121.0 / 16) * t * t
                val w = (121.0 / 4) * k * (t - 6.0 / 11.0).pow(2) + 1 - k
                val r = 121 * k * k * (t - 9.0 / 11.0).pow(2) + 1 - k * k
                val u = 484 * k * k * k * (t - 10.5 / 11.0).pow(2) + 1 - k * k * k
                min(min(q, w), min(r, u))
            }
        }
    }
}
