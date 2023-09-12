package net.casual.arcade.scheduler

import net.casual.arcade.scheduler.MinecraftTimeUnit.Ticks
import net.casual.arcade.utils.TimeUtils.Ticks

public class MinecraftTimeDuration private constructor(
    private val duration: Int,
    private val unit: MinecraftTimeUnit
) {
    public fun isZero(): Boolean {
        return this.duration == 0
    }

    public fun toTicks(): Int {
        return this.unit.toTicks(this.duration)
    }

    public fun toRedstoneTicks(): Double {
        return this.unit.toRedstoneTicks(this.duration)
    }

    public fun toMinecraftDays(): Double {
        return this.unit.toMinecraftDays(this.duration)
    }

    public fun toMilliseconds(): Int {
        return this.toTicks() * 50
    }

    public fun toSeconds(): Double {
        return this.unit.toSeconds(this.duration)
    }

    public fun toMinutes(): Double {
        return this.unit.toMinutes(this.duration)
    }

    public fun toHours(): Double {
        return this.unit.toHours(this.duration)
    }

    public fun toDays(): Double {
        return this.unit.toDays(this.duration)
    }

    public operator fun plus(other: MinecraftTimeDuration): MinecraftTimeDuration {
        return MinecraftTimeDuration(this.toTicks() + other.toTicks(), Ticks)
    }

    public operator fun minus(other: MinecraftTimeDuration): MinecraftTimeDuration {
        return MinecraftTimeDuration(this.toTicks() - other.toTicks(), Ticks)
    }

    public operator fun div(other: MinecraftTimeDuration): Int {
        return this.toTicks() / other.toTicks()
    }

    public operator fun compareTo(other: MinecraftTimeDuration): Int {
        return this.toTicks().compareTo(other.toTicks())
    }

    public companion object {
        public val ZERO: MinecraftTimeDuration = 0.Ticks

        @JvmStatic
        public fun of(duration: Int, unit: MinecraftTimeUnit): MinecraftTimeDuration {
            if (duration < 0) {
                throw IllegalArgumentException("Cannot have a negative duration!")
            }
            return MinecraftTimeDuration(duration, unit)
        }
    }
}