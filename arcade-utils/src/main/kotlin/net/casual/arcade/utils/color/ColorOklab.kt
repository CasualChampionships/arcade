/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.color

import kotlin.math.cbrt
import kotlin.math.pow

public data class ColorOklab(
    val l: Double,
    val a: Double,
    val b: Double
) {
    public companion object {
        public fun from(color: Int): ColorOklab {
            return this.from(ColorARGB(color))
        }

        public fun from(color: ColorARGB): ColorOklab {
            return this.from(color.red(), color.green(), color.blue())
        }

        public fun from(r: Int, g: Int, b: Int): ColorOklab {
            val lr = this.fromSRGB(r / 255.0)
            val lg = this.fromSRGB(g / 255.0)
            val lb = this.fromSRGB(b / 255.0)

            val l = cbrt(0.4122214708 * lr + 0.5363325363 * lg + 0.0514459929 * lb)
            val m = cbrt(0.2119034982 * lr + 0.6806995451 * lg + 0.1073969566 * lb)
            val s = cbrt(0.0883024619 * lr + 0.2817188376 * lg + 0.6299787005 * lb)

            return ColorOklab(
                0.2104542553 * l + 0.7936177850 * m - 0.0040720468 * s,
                1.9779984951 * l - 2.4285922050 * m + 0.4505937099 * s,
                0.0259040371 * l + 0.7827717662 * m - 0.8086757660 * s
            )
        }

        internal fun fromSRGB(x: Double): Double {
            return if (x <= 0.04045) x / 12.92 else ((x + 0.055) / 1.055).pow(2.4)
        }

        internal fun toSRGB(x: Double): Double {
            return if (x <= 0.0031308) x * 12.92 else 1.055 * x.pow(1.0 / 2.4) - 0.055
        }
    }
}
