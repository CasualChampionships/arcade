package net.casual.arcade.scheduler

import kotlin.math.roundToInt

enum class MinecraftTimeUnit(
    private val ticks: Int
) {
    Ticks(1),
    RedstoneTicks(2),
    MinecraftDays(24000),

    Seconds(20),
    Minutes(Seconds.toTicks(60)),
    Hours(Minutes.toTicks(60)),
    Days(Hours.toTicks(24));

    fun toTicks(duration: Int): Int {
        return this.ticks * duration
    }

    fun toTicks(duration: Number): Int {
        return (this.ticks * duration.toDouble()).roundToInt()
    }

    fun toRedstoneTicks(duration: Number): Double {
        return this.toTicks(duration.toDouble()) / RedstoneTicks.ticks.toDouble()
    }

    fun toMinecraftDays(duration: Number): Double {
        return this.toTicks(duration.toDouble()) / MinecraftDays.ticks.toDouble()
    }

    fun toSeconds(duration: Number): Double {
        return this.toTicks(duration.toDouble()) / Seconds.ticks.toDouble()
    }

    fun toMinutes(duration: Number): Double {
        return this.toTicks(duration.toDouble()) / Minutes.ticks.toDouble()
    }

    fun toHours(duration: Number): Double {
        return this.toTicks(duration.toDouble()) / Hours.ticks.toDouble()
    }

    fun toDays(duration: Number): Double {
        return this.toTicks(duration.toDouble()) / Days.ticks.toDouble()
    }

    fun duration(duration: Int): MinecraftTimeDuration {
        return MinecraftTimeDuration.of(duration, this)
    }
}