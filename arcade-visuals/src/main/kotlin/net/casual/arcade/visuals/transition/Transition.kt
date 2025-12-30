/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.transition

import net.casual.arcade.utils.TimeUtils.Seconds
import net.casual.arcade.utils.coroutine.delay
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.minecraft.server.level.ServerPlayer
import org.jetbrains.annotations.ApiStatus.NonExtendable
import org.jetbrains.annotations.ApiStatus.OverrideOnly
import kotlin.math.roundToInt

public interface Transition {
    @OverrideOnly
    public fun beforeTransition(
        players: Collection<ServerPlayer>,
        interval: MinecraftTimeDuration,
        updates: Int
    ) {

    }

    @OverrideOnly
    public fun updateTransition(
        players: Collection<ServerPlayer>,
        current: Int,
        total: Int,
        remaining: MinecraftTimeDuration
    )

    @OverrideOnly
    public fun afterTransition(players: Collection<ServerPlayer>) {

    }

    @NonExtendable
    public suspend fun transition(
        duration: MinecraftTimeDuration = 10.Seconds,
        interval: MinecraftTimeDuration = 1.Seconds,
        players: () -> Collection<ServerPlayer>
    ) {
        var remaining = duration
        val total = (remaining / interval).roundToInt()
        var current = 0
        this.beforeTransition(players.invoke(), interval, total)
        while (remaining > MinecraftTimeDuration.ZERO) {
            this.updateTransition(players.invoke(), current++, total, remaining)
            remaining -= interval
            if (remaining > MinecraftTimeDuration.ZERO) {
                delay(interval)
            }
        }
        this.afterTransition(players.invoke())
    }
}