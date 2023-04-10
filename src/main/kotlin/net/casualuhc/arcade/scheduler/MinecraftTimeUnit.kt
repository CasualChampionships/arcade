package net.casualuhc.arcade.scheduler

import kotlin.math.roundToInt

@Suppress("unused", "MemberVisibilityCanBePrivate")
enum class MinecraftTimeUnit(
    val ticksPerUnit: Int
) {
    Ticks(1),
    RedstoneTicks(2),
    MinecraftDays(24000),

    Seconds(20),
    Minutes(Seconds.toTicks(60)),
    Hours(Minutes.toTicks(60)),
    Days(Hours.toTicks(24));

    fun toTicks(unitTime: Int): Int {
        return this.ticksPerUnit * unitTime
    }

    fun toTicks(unitTime: Double): Int {
        return (this.ticksPerUnit * unitTime).roundToInt()
    }
}