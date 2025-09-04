/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.color

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import net.casual.arcade.utils.MathUtils
import net.minecraft.Util
import net.minecraft.util.ARGB
import java.util.*
import java.util.stream.IntStream

@JvmInline
public value class ColorARGB(private val data: Int) {
    public fun alpha(): Int {
        return ARGB.alpha(this.data)
    }

    public fun red(): Int {
        return ARGB.red(this.data)
    }

    public fun green(): Int {
        return ARGB.green(this.data)
    }

    public fun blue(): Int {
        return ARGB.blue(this.data)
    }

    public fun color(): Int {
        return this.data
    }

    public fun format(): String {
        return String.format(Locale.ROOT, "#%08X", this.data)
    }

    public fun with(
        alpha: Int = this.alpha(),
        red: Int = this.red(),
        green: Int = this.green(),
        blue: Int = this.blue()
    ): ColorARGB {
        return from(alpha, red, green, blue)
    }

    public operator fun component1(): Int {
        return this.alpha()
    }

    public operator fun component2(): Int {
        return this.red()
    }

    public operator fun component3(): Int {
        return this.green()
    }

    public operator fun component4(): Int {
        return this.blue()
    }

    public companion object {
        public val HEX_CODEC: Codec<ColorARGB> = Codec.STRING.comapFlatMap(Companion::parse, ColorARGB::format)!!
        public val ARRAY_CODEC: Codec<ColorARGB> = Codec.INT_STREAM.comapFlatMap(
            { stream -> Util.fixedSize(stream, 4).map { arr -> from(arr[0], arr[1], arr[2], arr[3]) } },
            { color -> IntStream.of(color.alpha(), color.red(), color.green(), color.blue()) }
        )
        public val INT_CODEC: Codec<ColorARGB> = Codec.INT.xmap(::ColorARGB, ColorARGB::color)

        public val CODEC: Codec<ColorARGB> = Codec.withAlternative(
            HEX_CODEC, Codec.withAlternative(ARRAY_CODEC, INT_CODEC)
        )

        public fun from(alpha: Double, red: Double, green: Double, blue: Double): ColorARGB {
            return this.from(
                (alpha * 255).toInt().coerceIn(0, 255),
                (red * 255).toInt().coerceIn(0, 255),
                (green * 255).toInt().coerceIn(0, 255),
                (blue * 255).toInt().coerceIn(0, 255),
            )
        }

        public fun from(alpha: Int, red: Int, green: Int, blue: Int): ColorARGB {
            return ColorARGB(ARGB.color(alpha, red, green, blue))
        }

        public fun from(color: ColorOklab): ColorARGB {
            val l = MathUtils.cube(color.l + 0.3963377774 * color.a + 0.2158037573 * color.b)
            val m = MathUtils.cube(color.l - 0.1055613458 * color.a - 0.0638541728 * color.b)
            val s = MathUtils.cube(color.l - 0.0894841775 * color.a - 1.2914855480 * color.b)

            val lr = +4.0767416621 * l - 3.3077115913 * m + 0.2309699292 * s
            val lg = -1.2684380046 * l + 2.6097574011 * m - 0.3413193965 * s
            val lb = -0.0041960863 * l - 0.7034186147 * m + 1.7076147010 * s

            return this.from(
                1.0,
                ColorOklab.toSRGB(lr).coerceIn(0.0, 1.0),
                ColorOklab.toSRGB(lg).coerceIn(0.0, 1.0),
                ColorOklab.toSRGB(lb).coerceIn(0.0, 1.0)
            )
        }

        public fun parse(string: String): DataResult<ColorARGB> {
            if (!string.startsWith("#")) {
                return DataResult.error { "Not a color code: $string" }
            }
            try {
                val data = string.substring(1).toLong(16)
                return DataResult.success(ColorARGB(data.toInt()))
            } catch (e: NumberFormatException) {
                return DataResult.error { "Exception parsing color code: ${e.message}" }
            }
        }
    }
}