package net.casual.arcade.utils

import net.casual.arcade.gui.screen.SelectionScreenBuilder
import net.casual.arcade.gui.screen.SelectionScreenComponents
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.settings.DisplayableGameSetting
import net.casual.arcade.utils.PlayerUtils.location
import net.casual.arcade.utils.PlayerUtils.teleportTo
import net.casual.arcade.utils.TeamUtils.getServerPlayers
import net.minecraft.network.chat.Component
import net.minecraft.world.MenuProvider
import net.minecraft.world.item.ItemStack
import net.minecraft.world.scores.PlayerTeam

object ScreenUtils {
    fun createSpectatorScreen(
        components: SelectionScreenComponents = DefaultSpectatorScreenComponent,
        teamFilter: (PlayerTeam) -> Boolean = { true },
        teamIcon: (PlayerTeam) -> ItemStack = TeamUtils::colouredHeadForTeam
    ): MenuProvider {
        val builder = SelectionScreenBuilder(components)
        val teams = TeamUtils.teams()
        for (team in teams) {
            if (teamFilter(team)) {
                builder.selection(teamIcon(team)) { player ->
                    player.openMenu(createTeamScreen(team, components))
                }
            }
        }
        return builder.build()
    }

    fun createTeamScreen(
        team: PlayerTeam,
        components: SelectionScreenComponents = DefaultSpectatorScreenComponent
    ): MenuProvider {
        val builder = SelectionScreenBuilder(components)
        for (teammate in team.getServerPlayers()) {
            builder.selection(ItemUtils.generatePlayerHead(teammate.scoreboardName)) { player ->
                player.teleportTo(teammate.location)
            }
        }
        return builder.build()
    }

    fun createMinigameRulesScreen(
        minigame: Minigame,
        components: SelectionScreenComponents = DefaultMinigameScreenComponent
    ): MenuProvider {
        val builder = SelectionScreenBuilder(components)
        for (display in minigame.settings.values) {
            builder.selection(display.display) { player ->
                player.openMenu(createSettingMenu(display, components))
            }
        }
        return builder.build()
    }

    fun <T: Any> createSettingMenu(
        display: DisplayableGameSetting<T>,
        components: SelectionScreenComponents = DefaultMinigameScreenComponent
    ): MenuProvider {
        val builder = SelectionScreenBuilder(components)
        for ((option, value) in display.options) {
            builder.selection(option) {
                display.setting.set(value)
            }
        }
        return builder.build()
    }

    private object DefaultSpectatorScreenComponent: SelectionScreenComponents {
        override fun getTitle(): Component {
            return Component.literal("Spectator Screen")
        }
    }

    private object DefaultMinigameScreenComponent: SelectionScreenComponents {
        override fun getTitle(): Component {
            return Component.literal("Minigame Settings Screen")
        }
    }
}