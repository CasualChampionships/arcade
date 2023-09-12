package net.casual.arcade.scheduler

import kotlin.math.roundToInt

public enum class MinecraftTimeUnit(
    private val ticks: Int
) {
    Ticks(1),
    RedstoneTicks(2),
    MinecraftDays(24000),

    Seconds(20),
    Minutes(Seconds.toTicks(60)),
    Hours(Minutes.toTicks(60)),
    Days(Hours.toTicks(24));

    public fun toTicks(duration: Int): Int {
        return this.ticks * duration
    }

    public fun toTicks(duration: Number): Int {
        return (this.ticks * duration.toDouble()).roundToInt()
    }

    public fun toRedstoneTicks(duration: Number): Double {
        return this.toTicks(duration.toDouble()) / RedstoneTicks.ticks.toDouble()
    }

    public fun toMinecraftDays(duration: Number): Double {
        return this.toTicks(duration.toDouble()) / MinecraftDays.ticks.toDouble()
    }

    public fun toSeconds(duration: Number): Double {
        return this.toTicks(duration.toDouble()) / Seconds.ticks.toDouble()
    }

    public fun toMinutes(duration: Number): Double {
        return this.toTicks(duration.toDouble()) / Minutes.ticks.toDouble()
    }

    public fun toHours(duration: Number): Double {
        return this.toTicks(duration.toDouble()) / Hours.ticks.toDouble()
    }

    public fun toDays(duration: Number): Double {
        return this.toTicks(duration.toDouble()) / Days.ticks.toDouble()
    }

    public fun duration(duration: Int): MinecraftTimeDuration {
        return MinecraftTimeDuration.of(duration, this)
    }
}