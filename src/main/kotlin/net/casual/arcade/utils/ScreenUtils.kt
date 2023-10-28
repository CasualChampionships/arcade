package net.casual.arcade.utils

import net.casual.arcade.gui.screen.SelectionScreenBuilder
import net.casual.arcade.gui.screen.SelectionScreenComponents
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.settings.DisplayableGameSetting
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.ItemUtils.enableGlint
import net.casual.arcade.utils.ItemUtils.removeEnchantments
import net.casual.arcade.utils.PlayerUtils.location
import net.casual.arcade.utils.PlayerUtils.teleportTo
import net.casual.arcade.utils.TeamUtils.getOnlinePlayers
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.MenuProvider
import net.minecraft.world.item.ItemStack
import net.minecraft.world.scores.PlayerTeam

public object ScreenUtils {
    public fun createSpectatorMenu(
        components: SelectionScreenComponents = DefaultSpectatorScreenComponent,
        teamFilter: (PlayerTeam) -> Boolean = { true },
        teamIcon: (PlayerTeam) -> ItemStack = TeamUtils::colouredHeadForTeam
    ): MenuProvider {
        val builder = SelectionScreenBuilder(components)
        val teams = TeamUtils.teams()
        val provider = builder.build()
        for (team in teams) {
            if (teamFilter(team)) {
                builder.selection(teamIcon(team)) { player ->
                    player.openMenu(createTeamMenu(team, components, provider) { it.isSpectator })
                }
            }
        }
        return provider
    }

    public fun createTeamMenu(
        team: PlayerTeam,
        components: SelectionScreenComponents = DefaultSpectatorScreenComponent,
        parent: MenuProvider? = null,
        predicate: (ServerPlayer) -> Boolean
    ): MenuProvider {
        val builder = SelectionScreenBuilder(components)
        builder.parent(parent)
        for (teammate in team.getOnlinePlayers()) {
            builder.selection(ItemUtils.generatePlayerHead(teammate.scoreboardName)) { player ->
                if (predicate(player)) {
                    player.teleportTo(teammate.location)
                }
            }
        }
        return builder.build()
    }

    public fun createMinigameSettingsMenu(
        minigame: Minigame<*>,
        components: SelectionScreenComponents = DefaultMinigameSettingsComponent,
        configComponents: (DisplayableGameSetting<*>) -> SelectionScreenComponents = ::DefaultMinigameConfigComponent
    ): MenuProvider {
        val builder = SelectionScreenBuilder(components)
        val provider = builder.build()
        for (display in minigame.gameSettings.values) {
            builder.selection(display.display) { player ->
                player.openMenu(createSettingConfigMenu(display, configComponents(display), provider))
            }
        }
        return provider
    }

    public fun <T: Any> createSettingConfigMenu(
        display: DisplayableGameSetting<T>,
        components: SelectionScreenComponents = DefaultMinigameConfigComponent(display),
        parent: MenuProvider? = null
    ): MenuProvider {
        val builder = SelectionScreenBuilder(components)
        builder.parent(parent)
        display.forEachOption { option, value ->
            builder.selection(option) {
                display.setting.set(value)
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

    public object DefaultSpectatorScreenComponent: SelectionScreenComponents {
        override fun getTitle(): Component {
            return "Spectator Screen".literal()
        }
    }

    public object DefaultMinigameSettingsComponent: SelectionScreenComponents {
        override fun getTitle(): Component {
            return "Minigame Settings Screen".literal()
        }
    }

    public class DefaultMinigameConfigComponent(
        private val setting: DisplayableGameSetting<*>
    ): SelectionScreenComponents {
        override fun getTitle(): Component {
            return setting.display.hoverName
        }
    }
}