package net.casual.arcade.utils

import net.casual.arcade.scheduler.MinecraftTimeDuration
import net.casual.arcade.scheduler.MinecraftTimeUnit
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

public object TimeUtils {
    public val Int.Ticks: MinecraftTimeDuration
        get() = MinecraftTimeUnit.Ticks.duration(this)

    public val Int.RedstoneTicks: MinecraftTimeDuration
        get() = MinecraftTimeUnit.RedstoneTicks.duration(this)

    public val Int.MinecraftDays: MinecraftTimeDuration
        get() = MinecraftTimeUnit.MinecraftDays.duration(this)

    public val Int.Seconds: MinecraftTimeDuration
        get() = MinecraftTimeUnit.Seconds.duration(this)

    public val Int.Minutes: MinecraftTimeDuration
        get() = MinecraftTimeUnit.Minutes.duration(this)

    public val Int.Hours: MinecraftTimeDuration
        get() = MinecraftTimeUnit.Hours.duration(this)

    public val Int.Days: MinecraftTimeDuration
        get() = MinecraftTimeUnit.Days.duration(this)

    public fun toEpoch(time: LocalTime, zone: ZoneId): Long {
        return time.toEpochSecond(LocalDate.now(zone), zone.rules.getOffset(Instant.now()))
    }

    public fun MinecraftTimeDuration.formatHHMMSS(): String {
        val seconds = this.seconds
        val hours = seconds / 3600
        return "%02d:".format(hours) + this.formatMMSS()
    }

    public fun MinecraftTimeDuration.formatMMSS(): String {
        val seconds = this.seconds
        val minutes = seconds % 3600 / 60
        val secs = seconds % 60
        return "%02d:%02d".format(minutes, secs)
    }

    public fun MinecraftTimeDuration.format(): String {
        val milliseconds = this.milliseconds
        if (milliseconds >= 3_600_000) {
            return this.formatHHMMSS()
        }
        if (milliseconds >= 600_000) {
            return this.formatMMSS()
        }
        val secs = (milliseconds % 60_000) / 1_000
        val millis = milliseconds % 1_000
        if (milliseconds < 60_000) {
            return "%d.%02d".format(secs, millis)
        }
        val minutes = milliseconds / 60_000
        return "%d:%02d.%02d".format(minutes, secs, millis)
    }
}