package net.casualuhc.arcade.utils

import net.casualuhc.arcade.screen.SelectionScreenBuilder
import net.casualuhc.arcade.utils.ItemUtils.literalNamed
import net.casualuhc.arcade.utils.PlayerUtils.location
import net.casualuhc.arcade.utils.PlayerUtils.teleportTo
import net.casualuhc.arcade.utils.TeamUtils.getServerPlayers
import net.minecraft.network.chat.Component
import net.minecraft.world.MenuProvider
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.scores.PlayerTeam

object ScreenUtils {
    fun createSpectatorScreen(
        title: Component = Component.literal("Spectator Screen"),
        previous: ItemStack = ItemStack(Items.RED_STAINED_GLASS).literalNamed("Previous"),
        next: ItemStack = ItemStack(Items.GREEN_STAINED_GLASS).literalNamed("Next"),
        filler: ItemStack = ItemStack(Items.GRAY_STAINED_GLASS).literalNamed("")
    ): MenuProvider {
        val builder = SelectionScreenBuilder()
            .title(title)
            .previous(previous)
            .next(next)
            .filler(filler)
        val teams = TeamUtils.teams()
        for (team in teams) {
            builder.selection(ItemStack(Items.BLUE_TERRACOTTA)) { player ->
                player.openMenu(createTeamScreen(team, title, previous, next, filler))
            }
        }
        return builder.build()
    }

    private fun createTeamScreen(
        team: PlayerTeam,
        title: Component,
        previous: ItemStack,
        next: ItemStack,
        filler: ItemStack
    ): MenuProvider {
        val builder = SelectionScreenBuilder()
            .title(title)
            .previous(previous)
            .next(next)
            .filler(filler)
        for (teammate in team.getServerPlayers()) {
            builder.selection(ItemUtils.generatePlayerHead(teammate.scoreboardName)) { player ->
                player.teleportTo(teammate.location)
            }
        }
        return builder.build()
    }
}