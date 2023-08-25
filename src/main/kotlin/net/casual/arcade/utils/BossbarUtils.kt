package net.casual.arcade.utils

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.player.PlayerCreatedEvent
import net.casual.arcade.events.player.PlayerLeaveEvent
import net.casual.arcade.events.player.PlayerTickEvent
import net.casual.arcade.gui.extensions.PlayerBossbarsExtension
import net.casual.arcade.utils.PlayerUtils.addExtension
import net.casual.arcade.utils.PlayerUtils.getExtension
import net.minecraft.server.level.ServerPlayer

internal object BossbarUtils {
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