package net.casual.arcade.utils.time

import kotlin.math.roundToInt

/**
 * This enum class represents the different time units that can be used
 * to represent time in Minecraft.
 */
public enum class MinecraftTimeUnit(
    private val ticks: Int
) {
    /**
     * Represents a single game-tick.
     */
    Ticks(1),

    /**
     * Represents a single redstone-tick, or two ticks.
     */
    RedstoneTicks(2),

    /**
     * Represents a single Minecraft day, or 24,000 ticks.
     */
    MinecraftDays(24000),

    /**
     * Represents a single second, or 20 ticks.
     */
    Seconds(20),

    /**
     * Represents a single minute, or 60 seconds.
     */
    Minutes(Seconds.toTicks(60)),

    /**
     * Represents a single hour, or 60 minutes.
     */
    Hours(Minutes.toTicks(60)),

    /**
     * Represents a single day, or 24 hours.
     */
    Days(Hours.toTicks(24));

    /**
     * This method will convert the given [duration] of this unit
     * to the number of ticks.
     *
     * @param duration The duration of this unit to convert to ticks.
     * @return The number of ticks in the given duration.
     */
    public fun toTicks(duration: Int): Int {
        return this.ticks * duration
    }

    /**
     * This method will convert the given [duration] of this unit
     * to the number of ticks.
     *
     * @param duration The duration of this unit to convert to ticks.
     * @return The number of ticks in the given duration.
     */
    public fun toTicks(duration: Number): Double {
        return this.ticks * duration.toDouble()
    }

    /**
     * This method will convert the given [duration] of this unit
     * to the number of redstone ticks.
     *
     * @param duration The duration of this unit to convert to redstone ticks.
     * @return The number of redstone ticks in the given duration.
     */
    public fun toRedstoneTicks(duration: Int): Int {
        return (this.toTicks(duration.toDouble()) / RedstoneTicks.ticks).roundToInt()
    }

    /**
     * This method will convert the given [duration] of this unit
     * to the number of redstone ticks.
     *
     * @param duration The duration of this unit to convert to redstone ticks.
     * @return The number of redstone ticks in the given duration.
     */
    public fun toRedstoneTicks(duration: Number): Double {
        return this.toTicks(duration.toDouble()) / RedstoneTicks.ticks
    }

    /**
     * This method will convert the given [duration] of this unit
     * to the number of Minecraft days.
     *
     * @param duration The duration of this unit to convert to Minecraft days.
     * @return The number of Minecraft days in the given duration.
     */
    public fun toMinecraftDays(duration: Int): Int {
        return (this.toTicks(duration.toDouble()) / MinecraftDays.ticks).roundToInt()
    }

    /**
     * This method will convert the given [duration] of this unit
     * to the number of Minecraft days.
     *
     * @param duration The duration of this unit to convert to Minecraft days.
     * @return The number of Minecraft days in the given duration.
     */
    public fun toMinecraftDays(duration: Number): Double {
        return this.toTicks(duration.toDouble()) / MinecraftDays.ticks
    }

    /**
     * This method will convert the given [duration] of this unit
     * to the number of seconds.
     *
     * @param duration The duration of this unit to convert to seconds.
     * @return The number of seconds in the given duration.
     */
    public fun toSeconds(duration: Int): Int {
        return (this.toTicks(duration.toDouble()) / Seconds.ticks).roundToInt()
    }

    /**
     * This method will convert the given [duration] of this unit
     * to the number of seconds.
     *
     * @param duration The duration of this unit to convert to seconds.
     * @return The number of seconds in the given duration.
     */
    public fun toSeconds(duration: Number): Double {
        return this.toTicks(duration.toDouble()) / Seconds.ticks
    }

    /**
     * This method will convert the given [duration] of this unit
     * to the number of minutes.
     *
     * @param duration The duration of this unit to convert to minutes.
     * @return The number of minutes in the given duration.
     */
    public fun toMinutes(duration: Int): Int {
        return (this.toTicks(duration.toDouble()) / Minutes.ticks).roundToInt()
    }

    /**
     * This method will convert the given [duration] of this unit
     * to the number of minutes.
     *
     * @param duration The duration of this unit to convert to minutes.
     * @return The number of minutes in the given duration.
     */
    public fun toMinutes(duration: Number): Double {
        return this.toTicks(duration.toDouble()) / Minutes.ticks
    }

    /**
     * This method will convert the given [duration] of this unit
     * to the number of hours.
     *
     * @param duration The duration of this unit to convert to hours.
     * @return The number of hours in the given duration.
     */
    public fun toHours(duration: Int): Int {
        return (this.toTicks(duration.toDouble()) / Hours.ticks).roundToInt()
    }

    /**
     * This method will convert the given [duration] of this unit
     * to the number of hours.
     *
     * @param duration The duration of this unit to convert to hours.
     * @return The number of hours in the given duration.
     */
    public fun toHours(duration: Number): Double {
        return this.toTicks(duration.toDouble()) / Hours.ticks
    }

    /**
     * This method will convert the given [duration] of this unit
     * to the number of days.
     *
     * @param duration The duration of this unit to convert to days.
     * @return The number of days in the given duration.
     */
    public fun toDays(duration: Int): Int {
        return (this.toTicks(duration.toDouble()) / Days.ticks).roundToInt()
    }

    /**
     * This method will convert the given [duration] of this unit
     * to the number of days.
     *
     * @param duration The duration of this unit to convert to days.
     * @return The number of days in the given duration.
     */
    public fun toDays(duration: Number): Double {
        return this.toTicks(duration.toDouble()) / Days.ticks
    }

    /**
     * This method will create a [MinecraftTimeDuration] of the given
     * [duration] in this unit.
     *
     * @param duration The duration of this unit.
     * @return The [MinecraftTimeDuration] of the given duration.
     */
    public fun duration(duration: Int): MinecraftTimeDuration {
        return MinecraftTimeDuration.of(duration, this)
    }
}