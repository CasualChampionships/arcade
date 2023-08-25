package net.casual.arcade.utils

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.player.PlayerCreatedEvent
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.PlayerMinigameExtension
import net.casual.arcade.gui.screen.SelectionScreenBuilder
import net.casual.arcade.settings.DisplayableGameSetting
import net.casual.arcade.utils.PlayerUtils.addExtension
import net.casual.arcade.utils.PlayerUtils.getExtension
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.MenuProvider

object MinigameUtils {
    internal val ServerPlayer.minigame
        get() = this.getExtension(PlayerMinigameExtension::class.java)

    fun ServerPlayer.getMinigame(): Minigame? {
        return this.minigame.getMinigame()
    }

    internal fun registerEvents() {
        GlobalEventHandler.register<PlayerCreatedEvent> { (player) ->
            player.addExtension(PlayerMinigameExtension(player))
        }
    }
}