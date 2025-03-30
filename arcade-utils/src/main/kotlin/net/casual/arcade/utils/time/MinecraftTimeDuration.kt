/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.time

import com.mojang.serialization.Codec
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.TimeUtils.format
import net.casual.arcade.utils.time.MinecraftTimeUnit.Ticks
import kotlin.math.max
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * This class represents a time duration with a give [MinecraftTimeUnit].
 *
 * @see MinecraftTimeUnit
 */
@JvmInline
public value class MinecraftTimeDuration private constructor(
    /**
     * This gets the duration of this time duration in ticks.
     */
    public val ticks: Int
) {
    /**
     * This gets whether the duration of this time duration is zero.
     */
    public val isZero: Boolean
        get() = this.ticks == 0

    /**
     * This gets the duration of this time duration in redstone ticks.
     */
    public val redstoneTicks: Int
        get() = Ticks.toRedstoneTicks(this.ticks)

    /**
     * This gets the duration of this time duration in Minecraft days.
     */
    public val minecraftDays: Int
        get() = Ticks.toMinecraftDays(this.ticks)

    /**
     * This gets the duration of this time duration in milliseconds.
     */
    public val milliseconds: Int
        get() = this.ticks * 50

    /**
     * This gets the duration of this time duration in seconds.
     */
    public val seconds: Int
        get() = Ticks.toSeconds(this.ticks)

    /**
     * This gets the duration of this time duration in minutes.
     */
    public val minutes: Int
        get() = Ticks.toMinutes(this.ticks)

    /**
     * This gets the duration of this time duration in hours.
     */
    public val hours: Int
        get() = Ticks.toHours(this.ticks)

    /**
     * This gets the duration of this time duration in days.
     */
    public val days: Int
        get() = Ticks.toDays(this.ticks)

    public val duration: Duration
        get() = milliseconds.milliseconds

    public operator fun plus(other: MinecraftTimeDuration): MinecraftTimeDuration {
        return MinecraftTimeDuration(this.ticks + other.ticks)
    }

    public operator fun minus(other: MinecraftTimeDuration): MinecraftTimeDuration {
        return MinecraftTimeDuration(max(this.ticks - other.ticks, 0))
    }

    public operator fun div(other: MinecraftTimeDuration): Double {
        return this.ticks / other.ticks.toDouble()
    }

    public operator fun times(other: Int): MinecraftTimeDuration {
        return MinecraftTimeDuration(max(this.ticks * other, 0))
    }

    public operator fun times(other: Double): MinecraftTimeDuration {
        return MinecraftTimeDuration(max(this.ticks * other, 0.0).toInt())
    }

    public operator fun compareTo(other: MinecraftTimeDuration): Int {
        return this.ticks.compareTo(other.ticks)
    }

    override fun toString(): String {
        return this.format()
    }

    public companion object {
        public val ZERO: MinecraftTimeDuration = 0.Ticks

        public val CODEC: Codec<MinecraftTimeDuration> = Codec.INT.xmap(Ticks::duration, MinecraftTimeDuration::ticks)

        @JvmStatic
        public fun of(duration: Int, unit: MinecraftTimeUnit): MinecraftTimeDuration {
            if (duration < 0) {
                return ZERO
            }
            return MinecraftTimeDuration(unit.toTicks(duration))
        }
    }
}