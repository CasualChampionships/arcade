package net.casualuhc.arcade.utils

import net.casualuhc.arcade.events.GlobalEventHandler
import net.casualuhc.arcade.events.player.PlayerCreatedEvent
import net.casualuhc.arcade.minigame.Minigame
import net.casualuhc.arcade.minigame.PlayerMinigameExtension
import net.casualuhc.arcade.screen.SelectionScreenBuilder
import net.casualuhc.arcade.settings.DisplayableGameSetting
import net.casualuhc.arcade.utils.PlayerUtils.addExtension
import net.casualuhc.arcade.utils.PlayerUtils.getExtension
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.MenuProvider

object MinigameUtils {
    internal val ServerPlayer.minigame
        get() = this.getExtension(PlayerMinigameExtension::class.java)

    fun ServerPlayer.getMinigame(): Minigame? {
        return this.minigame.getMinigame()
    }

    fun createRulesMenu(
        minigame: Minigame,
        modifier: SelectionScreenBuilder.() -> Unit
    ): MenuProvider {
        val builder = SelectionScreenBuilder()
        for (display in minigame.settings.values) {
            builder.selection(display.display) { player ->
                player.openMenu(createSettingMenu(display, modifier))
            }
        }
        modifier(builder)
        return builder.build()
    }

    private fun <T: Any> createSettingMenu(
        display: DisplayableGameSetting<T>,
        modifier: SelectionScreenBuilder.() -> Unit
    ): MenuProvider {
        return SelectionScreenBuilder().apply {
            for ((option, value) in display.options) {
                selection(option) {
                    display.setting.set(value)
                }
            }
            modifier()
        }.build()
    }

    init {
        GlobalEventHandler.register<PlayerCreatedEvent> { (player) ->
            player.addExtension(PlayerMinigameExtension(player))
        }
    }
}