/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.dimensions.level.clock

import net.minecraft.util.Mth
import net.minecraft.world.clock.ClockNetworkState
import net.minecraft.world.clock.ClockState
import net.minecraft.world.level.gamerules.GameRules

public class LevelClockInstance(
    internal var totalTicks: Long = 0,
    internal var partialTick: Float = 0.0F,
    internal var rate: Float = 1.0F,
    internal var paused: Boolean = false
) {
    public fun tick() {
        if (!this.paused) {
            this.partialTick += this.rate
            val fullTicks = Mth.floor(this.partialTick)
            this.partialTick -= fullTicks
            this.totalTicks += fullTicks
        }
    }

    public fun packState(): ClockState {
        return ClockState(this.totalTicks, this.partialTick, this.rate, this.paused)
    }

    public fun packNetworkState(gamerules: GameRules): ClockNetworkState {
        val effectivelyPaused = this.paused || !gamerules.get(GameRules.ADVANCE_TIME)
        return ClockNetworkState(this.totalTicks, this.partialTick, if (effectivelyPaused) 0.0F else this.rate)
    }

    public companion object {
        public fun from(state: ClockState): LevelClockInstance {
            return LevelClockInstance(state.totalTicks, state.partialTick, state.rate, state.paused)
        }
    }
}