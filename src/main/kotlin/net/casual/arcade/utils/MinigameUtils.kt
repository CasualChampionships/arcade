package net.casual.arcade.utils

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.player.PlayerCreatedEvent
import net.casual.arcade.gui.countdown.Countdown
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.extensions.PlayerMinigameExtension
import net.casual.arcade.scheduler.MinecraftTimeDuration
import net.casual.arcade.task.Completable
import net.casual.arcade.utils.PlayerUtils.addExtension
import net.casual.arcade.utils.PlayerUtils.getExtension
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.scores.PlayerTeam

public object MinigameUtils {
    internal val ServerPlayer.minigame
        get() = this.getExtension(PlayerMinigameExtension::class.java)

    @JvmStatic
    public fun ServerPlayer.getMinigame(): Minigame<*>? {
        return this.minigame.getMinigame()
    }

    @JvmStatic
    public fun Countdown.countdown(minigame: Minigame<*>): Completable {
        val post = Completable.Impl()
        var remaining = this.getDuration()
        val interval = this.getInterval()
        var current = remaining / interval
        minigame.scheduler.schedulePhasedInLoop(MinecraftTimeDuration.ZERO, interval, remaining) {
            this.sendCountdown(minigame.getPlayers(), current--, remaining)
            remaining -= interval
        }
        minigame.scheduler.schedulePhased(remaining, post::complete)
        return post
    }

    internal fun registerEvents() {
        GlobalEventHandler.register<PlayerCreatedEvent> { (player) ->
            player.addExtension(PlayerMinigameExtension(player))
        }
    }
}