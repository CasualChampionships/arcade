/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.dimensions.level.clock

import net.casual.arcade.dimensions.level.extensions.LevelClockExtension
import net.casual.arcade.utils.isOf
import net.minecraft.core.Holder
import net.minecraft.network.protocol.game.ClientboundSetTimePacket
import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.world.clock.*
import java.util.stream.Stream
import kotlin.math.max

public class DelegatedLevelClockManager(
    private val default: ResourceKey<WorldClock>,
    private val extension: LevelClockExtension,
    private val wrapped: ServerClockManager
): ServerClockManager(PackedClockStates.EMPTY) {
    override fun init(server: MinecraftServer) {
        throw UnsupportedOperationException()
    }

    override fun packState(): PackedClockStates {
        throw UnsupportedOperationException()
    }

    override fun tick() {
        throw UnsupportedOperationException()
    }

    override fun setTotalTicks(clock: Holder<WorldClock>, totalTicks: Long) {
        if (this.isOverriddenWorldClock(clock)) {
            this.extension.modify { state -> ClockState(totalTicks, 0.0F, state.rate, state.paused) }
        } else {
            this.wrapped.setTotalTicks(clock, totalTicks)
        }
    }

    override fun moveToTimeMarker(clock: Holder<WorldClock>, timeMarkerId: ResourceKey<ClockTimeMarker>): Boolean {
        if (this.isOverriddenWorldClock(clock)) {
            var success = false
            this.extension.modify { state ->
                val marker = this.extension.getMarker(timeMarkerId)
                if (marker != null) {
                    success = true
                    ClockState(marker.resolveTimeToMoveTo(state.totalTicks), 0.0F, state.rate, state.paused)
                } else {
                    state
                }
            }
            return success
        } else {
            return this.wrapped.moveToTimeMarker(clock, timeMarkerId)
        }
    }

    override fun addTicks(clock: Holder<WorldClock>, ticks: Int) {
        if (this.isOverriddenWorldClock(clock)) {
            this.extension.modify { state ->
                ClockState(max(state.totalTicks + ticks, 0), state.partialTick, state.rate, state.paused)
            }
        } else {
            this.wrapped.addTicks(clock, ticks)
        }
    }

    override fun setPaused(clock: Holder<WorldClock>, paused: Boolean) {
        if (this.isOverriddenWorldClock(clock)) {
            this.extension.modify { state ->
                ClockState(state.totalTicks, state.partialTick, state.rate, paused)
            }
        } else {
            this.wrapped.setPaused(clock, paused)
        }
    }

    override fun setRate(clock: Holder<WorldClock>, rate: Float) {
        if (this.isOverriddenWorldClock(clock)) {
            this.extension.modify { state ->
                ClockState(state.totalTicks, state.partialTick, rate, state.paused)
            }
        } else {
            this.wrapped.setRate(clock, rate)
        }
    }

    override fun getTotalTicks(definition: Holder<WorldClock>): Long {
        if (this.isOverriddenWorldClock(definition)) {
            return this.extension.ticks()
        }
        return this.wrapped.getTotalTicks(definition)
    }

    override fun createFullSyncPacket(): ClientboundSetTimePacket {
        return this.wrapped.createFullSyncPacket()
    }

    override fun isAtTimeMarker(clock: Holder<WorldClock>, timeMarkerId: ResourceKey<ClockTimeMarker>): Boolean {
        if (this.isOverriddenWorldClock(clock)) {
            val marker = this.extension.getMarker(timeMarkerId)
            return marker != null && marker.occursAt(this.extension.ticks())
        }
        return this.wrapped.isAtTimeMarker(clock, timeMarkerId)
    }

    override fun commandTimeMarkersForClock(clock: Holder<WorldClock>): Stream<ResourceKey<ClockTimeMarker>> {
        return this.wrapped.commandTimeMarkersForClock(clock)
    }

    private fun isOverriddenWorldClock(clock: Holder<WorldClock>): Boolean {
        return clock.isOf(this.default) && this.extension.customized()
    }
}