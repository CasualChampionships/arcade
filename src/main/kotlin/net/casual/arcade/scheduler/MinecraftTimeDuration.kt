package net.casual.arcade.scheduler

import net.casual.arcade.scheduler.MinecraftTimeUnit.Ticks
import net.casual.arcade.utils.TimeUtils.Ticks
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


    @Deprecated(
        "Use the field getter instead",
        ReplaceWith("this.ticks")
    )
    public fun toTicks(): Int {
        return this.ticks
    }

    @Deprecated(
        "Use the field getter instead",
        ReplaceWith("this.redstoneTicks")
    )
    public fun toRedstoneTicks(): Int {
        return this.redstoneTicks
    }

    @Deprecated(
        "Use the field getter instead",
        ReplaceWith("this.minecraftDays")
    )
    public fun toMinecraftDays(): Int {
        return this.minecraftDays
    }

    @Deprecated(
        "Use the field getter instead",
        ReplaceWith("this.milliseconds")
    )
    public fun toMilliseconds(): Int {
        return this.milliseconds
    }

    @Deprecated(
        "Use the field getter instead",
        ReplaceWith("this.seconds")
    )
    public fun toSeconds(): Int {
        return this.seconds
    }

    @Deprecated(
        "Use the field getter instead",
        ReplaceWith("this.minutes")
    )
    public fun toMinutes(): Int {
        return this.minutes
    }

    @Deprecated(
        "Use the field getter instead",
        ReplaceWith("this.hours")
    )
    public fun toHours(): Int {
        return this.hours
    }

    @Deprecated(
        "Use the field getter instead",
        ReplaceWith("this.days")
    )
    public fun toDays(): Int {
        return this.days
    }

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

    public companion object {
        public val ZERO: MinecraftTimeDuration = 0.Ticks

        @JvmStatic
        public fun of(duration: Int, unit: MinecraftTimeUnit): MinecraftTimeDuration {
            if (duration < 0) {
                return ZERO
            }
            return MinecraftTimeDuration(unit.toTicks(duration))
        }
    }
}