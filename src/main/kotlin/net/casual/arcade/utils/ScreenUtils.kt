package net.casual.arcade.utils

import eu.pb4.sgui.api.elements.GuiElement
import eu.pb4.sgui.api.gui.GuiInterface
import eu.pb4.sgui.api.gui.SimpleGui
import net.casual.arcade.gui.screen.SelectionGuiBuilder
import net.casual.arcade.gui.screen.SelectionGuiComponents
import net.casual.arcade.settings.display.DisplayableSettings
import net.casual.arcade.settings.display.MenuGameSetting
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.PlayerUtils.location
import net.casual.arcade.utils.PlayerUtils.teleportTo
import net.casual.arcade.utils.TeamUtils.getOnlineCount
import net.casual.arcade.utils.TeamUtils.getOnlinePlayers
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.scores.PlayerTeam

public object ScreenUtils {
    public fun SelectionGuiBuilder.addSpectatablePlayers(
        players: Iterable<ServerPlayer> = PlayerUtils.players(),
        mapper: (ServerPlayer) -> ItemStack = { ItemUtils.generatePlayerHead(it.scoreboardName) }
    ): SelectionGuiBuilder {
        this.elements(players, mapper) { gui, player ->
            if (!player.isRemoved) {
                gui.player.teleportTo(player.location)
            }
        }
        return this
    }

    public fun SelectionGuiBuilder.addSpectatableTeams(
        teams: Iterable<PlayerTeam> = TeamUtils.teams().filter { it.getOnlineCount() > 0 },
        mapper: (PlayerTeam) -> ItemStack = { TeamUtils.colouredHeadForTeam(it) },
        generator: (GuiInterface, PlayerTeam) -> SelectionGuiBuilder = { gui, _ -> SelectionGuiBuilder(gui) }
    ): SelectionGuiBuilder {
        this.elements(teams, mapper) { gui, team ->
            val builder = generator.invoke(gui, team)
            builder.addSpectatablePlayers(team.getOnlinePlayers())
            builder.build().open()
        }
        return this
    }

    public fun SelectionGuiBuilder.addSettings(
        displays: DisplayableSettings,
        generator: (GuiInterface, MenuGameSetting<*>) -> SelectionGuiBuilder = { gui, _ -> SelectionGuiBuilder(gui) }
    ): SelectionGuiBuilder {
        val settings = displays.displays().toList()
        this.elements(settings.indices, { settings[it].display }) { gui, index ->
            createSettingsGui(gui, settings, index, generator).open()
        }
        return this
    }

    public fun <T: Any> SelectionGuiBuilder.addSettingOptions(
        setting: MenuGameSetting<T>
    ): SelectionGuiBuilder {
        for (option in setting.options) {
            this.element(option)
        }
        return this
    }

    private fun createSettingsGui(
        root: GuiInterface,
        settings: List<MenuGameSetting<*>>,
        index: Int,
        generator: (GuiInterface, MenuGameSetting<*>) -> SelectionGuiBuilder
    ): SimpleGui {
        val setting = settings[index]
        val builder = generator.invoke(root, setting)

        val previous = settings.getOrNull(index - 1)
        if (previous != null) {
            builder.menuElement(SelectionGuiBuilder.MenuSlot.FIRST, GuiElement(previous.display) { _, _, _, _ ->
                this.createSettingsGui(root, settings, index - 1, generator).open()
            })
        }
        val next = settings.getOrNull(index + 1)
        if (next != null) {
            builder.menuElement(SelectionGuiBuilder.MenuSlot.SIXTH, GuiElement(next.display) { _, _, _, _ ->
                this.createSettingsGui(root, settings, index + 1, generator).open()
            })
        }
        return builder.build()
    }

    public object DefaultSpectatorScreenComponent: SelectionGuiComponents {
        override val title: Component = "Spectator Screen".literal()
    }

    public object DefaultSettingsComponent: SelectionGuiComponents {
        override val title: Component = "Settings Screen".literal()
    }

    public object DefaultMinigameSettingsComponent: SelectionGuiComponents {
        override val title: Component = "Minigame Settings Screen".literal()
    }

    public class DefaultSettingsComponents(
        private val setting: MenuGameSetting<*>
    ): SelectionGuiComponents {
        override val title: Component = this.setting.display.hoverName
    }
}