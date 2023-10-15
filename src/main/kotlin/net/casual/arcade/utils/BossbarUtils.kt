package net.casual.arcade.utils

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.player.PlayerCreatedEvent
import net.casual.arcade.events.player.PlayerLeaveEvent
import net.casual.arcade.events.player.PlayerTickEvent
import net.casual.arcade.gui.bossbar.TimerBossBar
import net.casual.arcade.gui.extensions.PlayerBossbarsExtension
import net.casual.arcade.gui.task.BossBarTask
import net.casual.arcade.scheduler.MinecraftTimeDuration
import net.casual.arcade.utils.PlayerUtils.addExtension
import net.casual.arcade.utils.PlayerUtils.getExtension
import net.minecraft.server.level.ServerPlayer

public object BossbarUtils {
    internal val ServerPlayer.bossbars
        get() = this.getExtension(PlayerBossbarsExtension::class.java)


    public fun BossBarTask<TimerBossBar>.withDuration(duration: MinecraftTimeDuration): BossBarTask<TimerBossBar> {
        this.bar.setDuration(duration)
        return this
    }

    internal fun registerEvents() {
        GlobalEventHandler.register<PlayerCreatedEvent> { (player) ->
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