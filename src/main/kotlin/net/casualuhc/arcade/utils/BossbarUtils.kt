package net.casualuhc.arcade.utils

import net.casualuhc.arcade.events.GlobalEventHandler
import net.casualuhc.arcade.events.player.PlayerCreatedEvent
import net.casualuhc.arcade.events.player.PlayerLeaveEvent
import net.casualuhc.arcade.events.player.PlayerTickEvent
import net.casualuhc.arcade.gui.PlayerBossbarsExtension
import net.casualuhc.arcade.utils.PlayerUtils.addExtension
import net.casualuhc.arcade.utils.PlayerUtils.getExtension
import net.minecraft.server.level.ServerPlayer

object BossbarUtils {
    internal val ServerPlayer.bossbars
        get() = this.getExtension(PlayerBossbarsExtension::class.java)

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