package net.casual.arcade.utils

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.player.PlayerCreatedEvent
import net.casual.arcade.events.player.PlayerLeaveEvent
import net.casual.arcade.events.player.PlayerTickEvent
import net.casual.arcade.gui.extensions.PlayerTabDisplayExtension
import net.casual.arcade.utils.PlayerUtils.addExtension
import net.casual.arcade.utils.PlayerUtils.getExtension
import net.minecraft.server.level.ServerPlayer

object TabUtils {
    internal val ServerPlayer.tabDisplay
        get() = this.getExtension(PlayerTabDisplayExtension::class.java)

    internal fun registerEvents() {
        GlobalEventHandler.register<PlayerCreatedEvent> { (player) ->
            player.addExtension(PlayerTabDisplayExtension(player))
        }
        GlobalEventHandler.register<PlayerLeaveEvent> { (player) ->
            player.tabDisplay.disconnect()
        }
        GlobalEventHandler.register<PlayerTickEvent> { (player) ->
            player.tabDisplay.tick()
        }
    }
}