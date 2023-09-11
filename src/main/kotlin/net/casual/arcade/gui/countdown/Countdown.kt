package net.casual.arcade.gui.countdown

import net.casual.arcade.scheduler.MinecraftTimeDuration
import net.casual.arcade.scheduler.MinecraftTimeUnit.Seconds
import net.casual.arcade.utils.TimeUtils.Seconds
import net.minecraft.server.level.ServerPlayer
import org.jetbrains.annotations.ApiStatus.OverrideOnly

interface Countdown {
    fun getDuration(): MinecraftTimeDuration {
        return 10.Seconds
    }

    fun getInterval(): MinecraftTimeDuration {
        return 1.Seconds
    }

    @OverrideOnly
    fun sendCountdown(players: Collection<ServerPlayer>, current: Int, remaining: MinecraftTimeDuration)
}