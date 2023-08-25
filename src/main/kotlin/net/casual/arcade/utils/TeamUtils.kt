package net.casual.arcade.utils

import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue.Consumer
import net.casual.arcade.Arcade
import net.casual.arcade.extensions.Extension
import net.casual.arcade.extensions.ExtensionHolder
import net.casual.arcade.utils.ComponentUtils.unItalicise
import net.casual.arcade.utils.ExtensionUtils.addExtension
import net.casual.arcade.utils.ExtensionUtils.getExtension
import net.casual.arcade.utils.ExtensionUtils.getExtensions
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Team

@Suppress("unused")
object TeamUtils {
    @JvmStatic
    fun teams(): Collection<PlayerTeam> {
        return Arcade.server.scoreboard.playerTeams
    }

    @JvmStatic
    fun forEachTeam(consumer: Consumer<PlayerTeam>) {
        for (team in this.teams()) {
            consumer.accept(team)
        }
    }

    @JvmStatic
    fun Team.asPlayerTeam(): PlayerTeam {
        return this as PlayerTeam
    }

    @JvmStatic
    fun Team.getServerPlayers(): List<ServerPlayer> {
        val players = Arcade.server.playerList
        val team = ArrayList<ServerPlayer>()
        for (name in this.players) {
            val player = players.getPlayerByName(name)
            if (player != null) {
                team.add(player)
            }
        }
        return team
    }

    @JvmStatic
    fun colouredHeadForTeam(team: Team): ItemStack {
        val texture = when (team.color) {
            ChatFormatting.BLACK -> HeadTextures.BLACK
            ChatFormatting.DARK_BLUE -> HeadTextures.DARK_BLUE
            ChatFormatting.DARK_GREEN -> HeadTextures.DARK_GREEN
            ChatFormatting.DARK_AQUA -> HeadTextures.DARK_AQUA
            ChatFormatting.DARK_RED -> HeadTextures.DARK_RED
            ChatFormatting.DARK_PURPLE -> HeadTextures.DARK_PURPLE
            ChatFormatting.GOLD -> HeadTextures.GOLD
            ChatFormatting.GRAY -> HeadTextures.GRAY
            ChatFormatting.DARK_GRAY -> HeadTextures.DARK_GRAY
            ChatFormatting.BLUE -> HeadTextures.BLUE
            ChatFormatting.GREEN -> HeadTextures.GREEN
            ChatFormatting.AQUA -> HeadTextures.AQUA
            ChatFormatting.RED -> HeadTextures.RED
            ChatFormatting.LIGHT_PURPLE -> HeadTextures.LIGHT_PURPLE
            ChatFormatting.YELLOW -> HeadTextures.YELLOW
            else -> HeadTextures.WHITE
        }
        val item = ItemUtils.generatePlayerHead("Dummy", texture)
        return item.setHoverName(Component.literal(team.name).unItalicise())
    }

    @JvmStatic
    fun Team.addExtension(extension: Extension) {
        (this as ExtensionHolder).addExtension(extension)
    }

    @JvmStatic
    fun <T: Extension> Team.getExtension(type: Class<T>): T {
        return (this as ExtensionHolder).getExtension(type)
    }

    @JvmStatic
    fun Team.getExtensions(): Collection<Extension> {
        return (this as ExtensionHolder).getExtensions()
    }
}