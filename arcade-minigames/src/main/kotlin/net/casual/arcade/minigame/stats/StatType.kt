/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.stats

import com.mojang.serialization.Codec

public class StatType<T>(
    public val default: T,
    public val codec: Codec<T>
) {
    public companion object {
        public fun bool(default: Boolean = false): StatType<Boolean> {
            return StatType(default, Codec.BOOL)
        }

        public fun int32(default: Int = 0): StatType<Int> {
            return StatType(default, Codec.INT)
        }

        public fun int64(default: Long = 0): StatType<Long> {
            return StatType(default, Codec.LONG)
        }

        public fun float32(default: Float = 0.0F): StatType<Float> {
            return StatType(default, Codec.FLOAT)
        }

        public fun float64(default: Double = 0.0): StatType<Double> {
            return StatType(default, Codec.DOUBLE)
        }
    }
}