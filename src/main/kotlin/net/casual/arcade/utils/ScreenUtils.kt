package net.casual.arcade.utils

import net.casual.arcade.gui.screen.SelectionScreenBuilder
import net.casual.arcade.gui.screen.SelectionScreenComponents
import net.casual.arcade.gui.screen.SelectionScreenStyle
import net.casual.arcade.settings.display.DisplayableGameSetting
import net.casual.arcade.settings.display.DisplayableSettings
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.ItemUtils.enableGlint
import net.casual.arcade.utils.ItemUtils.removeEnchantments
import net.casual.arcade.utils.PlayerUtils.location
import net.casual.arcade.utils.PlayerUtils.teleportTo
import net.casual.arcade.utils.TeamUtils.getOnlineCount
import net.casual.arcade.utils.TeamUtils.getOnlinePlayers
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.MenuProvider
import net.minecraft.world.item.ItemStack
import net.minecraft.world.scores.PlayerTeam

public object ScreenUtils {
    public fun createSpectatorMenu(
        components: SelectionScreenComponents = DefaultSpectatorScreenComponent,
        parent: MenuProvider? = null,
        style: SelectionScreenStyle = SelectionScreenStyle.DEFAULT,
        teamStyle: (PlayerTeam) -> SelectionScreenStyle = { SelectionScreenStyle.DEFAULT },
        teamFilter: (PlayerTeam) -> Boolean = { true },
        teamIcon: (PlayerTeam) -> ItemStack = TeamUtils::colouredHeadForTeam
    ): MenuProvider {
        val builder = SelectionScreenBuilder(components)
        builder.parent(parent)
        builder.style(style)
        val teams = TeamUtils.teams()
        val provider = builder.build()
        for (team in teams) {
            if (team.getOnlineCount() > 0 && teamFilter(team)) {
                builder.selection(teamIcon(team)) { player ->
                    player.openMenu(createTeamMenu(team, components, provider, teamStyle(team)) { it.isSpectator })
                }
            }
        }
        return provider
    }

    public fun createTeamMenu(
        team: PlayerTeam,
        components: SelectionScreenComponents = DefaultSpectatorScreenComponent,
        parent: MenuProvider? = null,
        style: SelectionScreenStyle = SelectionScreenStyle.DEFAULT,
        predicate: (ServerPlayer) -> Boolean
    ): MenuProvider {
        val builder = SelectionScreenBuilder(components)
        builder.parent(parent)
        builder.style(style)
        for (teammate in team.getOnlinePlayers()) {
            builder.selection(ItemUtils.generatePlayerHead(teammate.scoreboardName)) { player ->
                if (predicate(player)) {
                    player.teleportTo(teammate.location)
                }
            }
        }
        return builder.build()
    }

    public fun createSettingsMenu(
        settings: DisplayableSettings,
        components: SelectionScreenComponents = DefaultSettingsComponent,
        parent: MenuProvider? = null,
        style: SelectionScreenStyle = SelectionScreenStyle.DEFAULT,
        configComponents: (DisplayableGameSetting<*>) -> SelectionScreenComponents = ::DefaultSettingsComponents,
        configStyle: (DisplayableGameSetting<*>) -> SelectionScreenStyle = ::createCenteredSettingStyle,
        modifiable: (ServerPlayer) -> Boolean = { true }
    ): MenuProvider {
        val builder = SelectionScreenBuilder(components)
        builder.parent(parent)
        builder.style(style)
        val provider = builder.build()
        for (display in settings.displays()) {
            builder.selection(display.display) { player ->
                player.openMenu(createSettingConfigMenu(display, configComponents(display), provider, configStyle(display), modifiable))
            }
        }
        return provider
    }

    public fun <T: Any> createSettingConfigMenu(
        display: DisplayableGameSetting<T>,
        components: SelectionScreenComponents = DefaultSettingsComponents(display),
        parent: MenuProvider? = null,
        style: SelectionScreenStyle = SelectionScreenStyle.DEFAULT,
        modifiable: (ServerPlayer) -> Boolean = { true }
    ): MenuProvider {
        val builder = SelectionScreenBuilder(components)
        builder.style(style)
        builder.parent(parent)
        display.forEachOption { option, value ->
            builder.selection(option) {
                if (modifiable(it)) {
                    display.setting.set(value)
                }
            }
        }
        builder.ticker { stack ->
            if (stack.isEnchanted) {
                if (display.setting.get() != display.getValue(stack.copy().removeEnchantments())) {
                    stack.removeEnchantments()
                }
            } else if (display.setting.get() == display.getValue(stack)) {
                stack.enableGlint()
            }
        }
        return builder.build()
    }

    private fun createCenteredSettingStyle(setting: DisplayableGameSetting<*>): SelectionScreenStyle {
        return SelectionScreenStyle.centered(setting.optionCount)
    }

    public object DefaultSpectatorScreenComponent: SelectionScreenComponents {
        override fun getTitle(): Component {
            return "Spectator Screen".literal()
        }
    }

    public object DefaultSettingsComponent: SelectionScreenComponents {
        override fun getTitle(): Component {
            return "Settings Screen".literal()
        }
    }

    public object DefaultMinigameSettingsComponent: SelectionScreenComponents {
        override fun getTitle(): Component {
            return "Minigame Settings Screen".literal()
        }
    }

    public class DefaultSettingsComponents(
        private val setting: DisplayableGameSetting<*>
    ): SelectionScreenComponents {
        override fun getTitle(): Component {
            return setting.display.hoverName
        }
    }
}