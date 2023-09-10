package net.casual.arcade.utils.minigame

import net.casual.arcade.minigame.Minigame
import net.casual.arcade.scheduler.MinecraftTimeDuration
import net.casual.arcade.scheduler.MinecraftTimeUnit.Seconds
import net.casual.arcade.scheduler.MinecraftTimeUnit.Ticks
import net.casual.arcade.task.Completable
import net.minecraft.server.level.ServerPlayer
import org.jetbrains.annotations.ApiStatus.NonExtendable
import org.jetbrains.annotations.ApiStatus.OverrideOnly

interface MinigameCountdown {
    fun getCountdownDuration(): MinecraftTimeDuration {
        return Seconds.duration(10)
    }

    fun getCountdownInterval(): MinecraftTimeDuration {
        return Seconds.duration(1)
    }

    @OverrideOnly
    fun sendCountdown(players: Collection<ServerPlayer>, remaining: MinecraftTimeDuration)

    @NonExtendable
    fun countdown(minigame: Minigame<*>): Completable {
        val post = Completable.Impl()
        var remaining = this.getCountdownDuration()
        val interval = this.getCountdownInterval()
        minigame.scheduler.schedulePhasedInLoop(MinecraftTimeDuration.ZERO, interval, remaining) {
            this.sendCountdown(minigame.getPlayers(), remaining)
            remaining -= interval
        }
        minigame.scheduler.schedulePhased(remaining, post::complete)
        return post
    }
}