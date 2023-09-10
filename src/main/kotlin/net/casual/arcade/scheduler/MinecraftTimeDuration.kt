package net.casual.arcade.scheduler

import net.casual.arcade.scheduler.MinecraftTimeUnit.Ticks

class MinecraftTimeDuration private constructor(
    private val duration: Int,
    private val unit: MinecraftTimeUnit
) {
    fun isZero(): Boolean {
        return this.duration == 0
    }

    fun toTicks(): Int {
        return this.unit.toTicks(this.duration)
    }

    fun toRedstoneTicks(): Double {
        return this.unit.toRedstoneTicks(this.duration)
    }

    fun toMinecraftDays(): Double {
        return this.unit.toMinecraftDays(this.duration)
    }

    fun toSeconds(): Double {
        return this.unit.toSeconds(this.duration)
    }

    fun toMinutes(): Double {
        return this.unit.toMinutes(this.duration)
    }

    fun toHours(): Double {
        return this.unit.toHours(this.duration)
    }

    fun toDays(): Double {
        return this.unit.toDays(this.duration)
    }

    operator fun plus(other: MinecraftTimeDuration): MinecraftTimeDuration {
        return MinecraftTimeDuration(this.toTicks() + other.toTicks(), Ticks)
    }

    operator fun minus(other: MinecraftTimeDuration): MinecraftTimeDuration {
        return MinecraftTimeDuration(this.toTicks() - other.toTicks(), Ticks)
    }

    operator fun compareTo(other: MinecraftTimeDuration): Int {
        return this.toTicks().compareTo(other.toTicks())
    }

    companion object {
        val ZERO = Ticks.duration(0)

        @JvmStatic
        fun of(duration: Int, unit: MinecraftTimeUnit): MinecraftTimeDuration {
            if (duration < 0) {
                throw IllegalArgumentException("Cannot have a negative duration!")
            }
            return MinecraftTimeDuration(duration, unit)
        }
    }
}