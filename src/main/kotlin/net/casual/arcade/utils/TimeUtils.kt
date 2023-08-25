package net.casual.arcade.utils

import net.casual.arcade.scheduler.MinecraftTimeUnit
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

object TimeUtils {
    fun toEpoch(time: LocalTime, zone: ZoneId): Long {
        return time.toEpochSecond(LocalDate.now(zone), zone.rules.getOffset(Instant.now()))
    }

    fun formatHHMMSS(time: Number, unit: MinecraftTimeUnit): String {
        val seconds = unit.toSeconds(time.toDouble()).toInt()
        val hours = seconds / 3600
        return "%02d:".format(hours) + this.formatMMSS(time, unit)
    }

    fun formatMMSS(time: Number, unit: MinecraftTimeUnit): String {
        val seconds = unit.toSeconds(time.toDouble()).toInt()
        val minutes = seconds % 3600 / 60
        val secs = seconds % 60
        return "%02d:%02d".format(minutes, secs)
    }
}