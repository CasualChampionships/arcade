package net.casual.arcade.gui.countdown

import net.casual.arcade.scheduler.MinecraftTimeDuration
import net.casual.arcade.utils.TimeUtils.Seconds
import net.minecraft.server.level.ServerPlayer
import org.jetbrains.annotations.ApiStatus.OverrideOnly

public interface Countdown {
    public fun getDuration(): MinecraftTimeDuration {
        return 10.Seconds
    }

    public fun getInterval(): MinecraftTimeDuration {
        return 1.Seconds
    }

    @OverrideOnly
    public fun sendCountdown(players: Collection<ServerPlayer>, current: Int, remaining: MinecraftTimeDuration)

    @OverrideOnly
    public fun afterCountdown(players: Collection<ServerPlayer>) {

    }
}