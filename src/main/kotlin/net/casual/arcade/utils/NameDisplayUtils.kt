package net.casual.arcade.utils

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.player.PlayerCreatedEvent
import net.casual.arcade.events.player.PlayerLeaveEvent
import net.casual.arcade.gui.extensions.PlayerNameScoreExtension
import net.casual.arcade.utils.PlayerUtils.addExtension
import net.casual.arcade.utils.PlayerUtils.getExtension
import net.minecraft.server.level.ServerPlayer

internal object NameDisplayUtils {
    internal val ServerPlayer.nameDisplay
        get() = this.getExtension(PlayerNameScoreExtension::class.java)

    internal fun registerEvents() {
        GlobalEventHandler.register<PlayerCreatedEvent> { (player) ->
            player.addExtension(PlayerNameScoreExtension(player))
        }
        GlobalEventHandler.register<PlayerLeaveEvent> { (player) ->
            player.nameDisplay.disconnect()
        }
    }
}