package net.casual.arcade.utils

import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue.Consumer
import net.casual.arcade.Arcade
import net.casual.arcade.extensions.Extension
import net.casual.arcade.extensions.ExtensionHolder
import net.casual.arcade.utils.ComponentUtils.unItalicise
import net.casual.arcade.utils.ExtensionUtils.addExtension
import net.casual.arcade.utils.ExtensionUtils.getExtension
import net.casual.arcade.utils.ExtensionUtils.getExtensions
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Team

@Suppress("unused")
object TeamUtils {
    @JvmStatic
    fun teams(): Collection<PlayerTeam> {
        return Arcade.getServer().scoreboard.playerTeams
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

    @Deprecated("Replaced with 'team.getOnlinePlayers()'", ReplaceWith("this.getOnlinePlayers()"))
    @JvmStatic
    fun Team.getServerPlayers(): List<ServerPlayer> {
        return this.getOnlinePlayers()
    }

    @JvmStatic
    fun Team.getOnlinePlayers(): List<ServerPlayer> {
        val team = ArrayList<ServerPlayer>()
        for (name in this.players) {
            val player = PlayerUtils.player(name)
            if (player != null) {
                team.add(player)
            }
        }
        return team
    }

    @JvmStatic
    fun Team.getOnlineCount(): Int {
        var count = 0
        for (name in this.players) {
            val player = PlayerUtils.player(name)
            if (player != null) {
                count++
            }
        }
        return count
    }

    @JvmStatic
    fun colouredHeadForTeam(team: Team): ItemStack {
        val head = ItemUtils.colouredHeadForFormatting(team.color)
        head.setHoverName(Component.literal(team.name).unItalicise())
        return head
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