package net.casual.arcade.gui.countdown

import net.casual.arcade.scheduler.MinecraftTimeDuration
import net.casual.arcade.utils.TimeUtils.Seconds
import net.minecraft.server.level.ServerPlayer
import org.jetbrains.annotations.ApiStatus.OverrideOnly

public interface Countdown {
    @OverrideOnly
    public fun beforeCountdown(players: Collection<ServerPlayer>, interval: MinecraftTimeDuration) {

    }

    @OverrideOnly
    public fun sendCountdown(players: Collection<ServerPlayer>, current: Int, remaining: MinecraftTimeDuration)

    @OverrideOnly
    public fun afterCountdown(players: Collection<ServerPlayer>) {

    }
}