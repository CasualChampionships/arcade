package net.casual.arcade.utils

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.player.PlayerExtensionEvent
import net.casual.arcade.events.player.PlayerLeaveEvent
import net.casual.arcade.events.player.PlayerTickEvent
import net.casual.arcade.gui.bossbar.TimerBossBar
import net.casual.arcade.gui.extensions.PlayerBossbarsExtension
import net.casual.arcade.minigame.task.impl.BossBarTask
import net.casual.arcade.scheduler.MinecraftTimeDuration
import net.casual.arcade.task.Task
import net.casual.arcade.task.serialization.TaskCreationContext
import net.casual.arcade.utils.PlayerUtils.addExtension
import net.casual.arcade.utils.PlayerUtils.getExtension
import net.minecraft.server.level.ServerPlayer

public object BossbarUtils {
    internal val ServerPlayer.bossbars
        get() = this.getExtension(PlayerBossbarsExtension::class.java)

    public fun <T: TimerBossBar> BossBarTask<T>.withDuration(duration: MinecraftTimeDuration): BossBarTask<T> {
        this.bar.setDuration(duration)
        return this
    }

    public fun <T: TimerBossBar> BossBarTask<T>.withRemainingDuration(duration: MinecraftTimeDuration): BossBarTask<T> {
        this.bar.setRemainingDuration(duration)
        return this
    }

    public fun <T: TimerBossBar> BossBarTask<T>.readData(context: TaskCreationContext): BossBarTask<T> {
        this.bar.readData(context)
        return this
    }

    public fun <T: TimerBossBar> BossBarTask<T>.then(task: Task): BossBarTask<T> {
        this.bar.then(task)
        return this
    }

    public fun shrink(percent: Float, factor: Float): Float {
        val shift = (1 - factor) / 2.0F
        return shift + percent * factor
    }

    internal fun registerEvents() {
        GlobalEventHandler.register<PlayerExtensionEvent> { (player) ->
            player.addExtension(PlayerBossbarsExtension(player))
        }
        GlobalEventHandler.register<PlayerLeaveEvent> { (player) ->
            player.bossbars.disconnect()
        }
        GlobalEventHandler.register<PlayerTickEvent> { (player) ->
            player.bossbars.tick()
        }
    }
}