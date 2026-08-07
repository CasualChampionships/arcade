/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.visuals.utils.elements.timer

import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.casual.arcade.virtual.visuals.data.DynamicVisualValues
import net.casual.arcade.virtual.visuals.elements.TickableElement
import net.casual.arcade.virtual.visuals.elements.UniversalElement
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer

/**
 * A countdown which can be displayed by any visual.
 *
 * The timer only counts down while it is being ticked, either by
 * registering it with [DynamicVisualValues.addTickable] or by calling
 * [tick] directly:
 * ```
 * val timer = TimerElement(30.Seconds)
 * bossbar.addTickable(timer)
 * bossbar.setProgress(timer.progress())
 * bossbar.setTitle(timer.remaining { Component.literal(it.formatHHMMSS()) })
 * ```
 *
 * The elements returned by [progress] and [remaining] only read the
 * timer, they never advance it.
 *
 * @param duration The duration to count down, or `null` for no duration.
 * @see TickableElement
 */
public class TimerElement(duration: MinecraftTimeDuration? = null): TickableElement {
    private val progress = UniversalElement { this.getProgress() }

    private var ticks: Int = -1
    private var tick: Int = 0

    /**
     * Whether the timer has finished counting down.
     */
    public var complete: Boolean = false
        private set

    /**
     * Whether this timer has a duration to count down.
     */
    public val hasDuration: Boolean
        get() = this.ticks != -1

    init {
        if (duration != null) {
            this.setTotalDuration(duration)
        }
    }

    override fun tick(server: MinecraftServer) {
        if (this.ticks == -1) {
            return
        }
        if (this.tick < this.ticks) {
            this.tick++
            return
        }
        this.complete = true
    }

    /**
     * Sets the duration of the timer, and restarts it.
     *
     * @param duration The duration to count down.
     */
    public fun setTotalDuration(duration: MinecraftTimeDuration) {
        this.complete = false
        this.tick = 0
        this.ticks = duration.ticks
    }

    /**
     * Sets the remaining duration of the timer, keeping
     * the total duration the same.
     *
     * @param duration The duration remaining.
     */
    public fun setRemainingDuration(duration: MinecraftTimeDuration) {
        if (!this.hasDuration) {
            return
        }
        this.tick = this.ticks - duration.ticks
    }

    /**
     * Removes the duration of the timer, stopping the countdown.
     */
    public fun removeDuration() {
        this.complete = true
        this.ticks = -1
    }

    /**
     * Gets the total duration on the timer.
     *
     * @return The total duration.
     */
    public fun getTotalDuration(): MinecraftTimeDuration {
        return if (!this.hasDuration) 0.Ticks else this.ticks.Ticks
    }

    /**
     * Gets the duration remaining on the timer.
     *
     * @return The duration remaining.
     */
    public fun getRemainingDuration(): MinecraftTimeDuration {
        return if (!this.hasDuration) 0.Ticks else (this.ticks - this.tick).Ticks
    }

    /**
     * Gets how far through the timer is, between `0.0` and `1.0`.
     *
     * @return The progress of the timer.
     */
    public fun getProgress(): Float {
        return if (!this.hasDuration) 0.0F else this.tick / this.ticks.toFloat()
    }

    /**
     * Gets an element generating the [getProgress] of this timer.
     *
     * @return The progress element.
     */
    public fun progress(): UniversalElement<Float> {
        return this.progress
    }

    /**
     * Gets an element generating a component from the
     * [getRemainingDuration] of this timer.
     *
     * @param formatter The formatter to generate the component with.
     * @return The remaining duration element.
     */
    public fun remaining(formatter: (MinecraftTimeDuration) -> Component): UniversalElement<Component> {
        return UniversalElement { formatter.invoke(this.getRemainingDuration()) }
    }
}