package net.casualuhc.arcade.utils

import net.casualuhc.arcade.events.GlobalEventHandler
import net.casualuhc.arcade.events.player.PlayerCreatedEvent
import net.casualuhc.arcade.events.player.PlayerLeaveEvent
import net.casualuhc.arcade.gui.extensions.PlayerNameScoreExtension
import net.casualuhc.arcade.utils.PlayerUtils.addExtension
import net.casualuhc.arcade.utils.PlayerUtils.getExtension
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