package net.casual.arcade.gui.countdown

import net.casual.arcade.scheduler.MinecraftTimeDuration
import net.casual.arcade.scheduler.MinecraftTimeUnit.Seconds
import net.minecraft.server.level.ServerPlayer
import org.jetbrains.annotations.ApiStatus.OverrideOnly

interface Countdown {
    fun getCountdownDuration(): MinecraftTimeDuration {
        return Seconds.duration(10)
    }

    fun getCountdownInterval(): MinecraftTimeDuration {
        return Seconds.duration(1)
    }

    @OverrideOnly
    fun sendCountdown(players: Collection<ServerPlayer>, current: Int, remaining: MinecraftTimeDuration)
}