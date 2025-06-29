/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.impl

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
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

    public companion object {
        public val HEX_CODEC: Codec<ColorARGB> = Codec.STRING.comapFlatMap(::parse, ColorARGB::format)!!
        public val ARRAY_CODEC: Codec<ColorARGB> = Codec.INT_STREAM.comapFlatMap(
            { stream -> Util.fixedSize(stream, 4).map { arr -> from(arr[0], arr[1], arr[2], arr[3]) } },
            { color -> IntStream.of(color.alpha(), color.red(), color.green(), color.blue()) }
        )
        public val INT_CODEC: Codec<ColorARGB> = Codec.INT.xmap(::ColorARGB, ColorARGB::color)

        public val CODEC: Codec<ColorARGB> = Codec.withAlternative(
            HEX_CODEC, Codec.withAlternative(ARRAY_CODEC, INT_CODEC)
        )

        public fun from(alpha: Int, red: Int, green: Int, blue: Int): ColorARGB {
            return ColorARGB(ARGB.color(alpha, red, green, blue))
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