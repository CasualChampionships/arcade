package net.casual.arcade.visuals.countdown

import net.casual.arcade.scheduler.GlobalTickedScheduler
import net.casual.arcade.scheduler.MinecraftScheduler
import net.casual.arcade.scheduler.task.Completable
import net.casual.arcade.utils.TimeUtils.Seconds
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.minecraft.server.level.ServerPlayer
import org.jetbrains.annotations.ApiStatus.NonExtendable
import org.jetbrains.annotations.ApiStatus.OverrideOnly
import kotlin.math.roundToInt

public interface Countdown {
    @OverrideOnly
    public fun beforeCountdown(players: Collection<ServerPlayer>, interval: MinecraftTimeDuration) {

    }

    @OverrideOnly
    public fun sendCountdown(players: Collection<ServerPlayer>, current: Int, remaining: MinecraftTimeDuration)

    @OverrideOnly
    public fun afterCountdown(players: Collection<ServerPlayer>) {

    }

    @NonExtendable
    public fun countdown(
        duration: MinecraftTimeDuration = 10.Seconds,
        interval: MinecraftTimeDuration = 1.Seconds,
        scheduler: MinecraftScheduler = GlobalTickedScheduler.asScheduler(),
        players: () -> Collection<ServerPlayer>
    ): Completable {
        val post = Completable.Impl()
        var remaining = duration
        var current = (remaining / interval).roundToInt()
        this.beforeCountdown(players(), interval)
        scheduler.scheduleInLoop(MinecraftTimeDuration.ZERO, interval, remaining) {
            this.sendCountdown(players(), current--, remaining)
            remaining -= interval
        }
        scheduler.schedule(remaining) {
            this.afterCountdown(players())
            post.complete()
        }
        return post
    }
}