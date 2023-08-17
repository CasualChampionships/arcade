package net.casualuhc.arcade.utils

import net.casualuhc.arcade.events.GlobalEventHandler
import net.casualuhc.arcade.events.player.PlayerCreatedEvent
import net.casualuhc.arcade.minigame.Minigame
import net.casualuhc.arcade.minigame.PlayerMinigameExtension
import net.casualuhc.arcade.utils.PlayerUtils.addExtension
import net.casualuhc.arcade.utils.PlayerUtils.getExtension
import net.minecraft.server.level.ServerPlayer

object MinigameUtils {
    internal val ServerPlayer.minigame
        get() = this.getExtension(PlayerMinigameExtension::class.java)

    fun ServerPlayer.getMinigame(): Minigame? {
        return this.minigame.getMinigame()
    }

    init {
        GlobalEventHandler.register<PlayerCreatedEvent> { (player) ->
            player.addExtension(PlayerMinigameExtension(player))
        }
    }
}