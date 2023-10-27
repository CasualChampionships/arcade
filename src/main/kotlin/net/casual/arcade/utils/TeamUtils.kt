package net.casual.arcade.utils

import com.google.common.collect.Iterators
import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue.Consumer
import net.casual.arcade.Arcade
import net.casual.arcade.extensions.Extension
import net.casual.arcade.extensions.ExtensionHolder
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.ComponentUtils.unitalicise
import net.casual.arcade.utils.ExtensionUtils.addExtension
import net.casual.arcade.utils.ExtensionUtils.getExtension
import net.casual.arcade.utils.ExtensionUtils.getExtensions
import net.minecraft.ChatFormatting
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Team

public object TeamUtils {
    public val TEAM_COLOURS: Set<ChatFormatting> = ChatFormatting.values().copyOfRange(0, 16).toSet()

    @JvmStatic
    public fun teams(): Collection<PlayerTeam> {
        return Arcade.getServer().scoreboard.playerTeams
    }

    @JvmStatic
    public fun forEachTeam(consumer: Consumer<PlayerTeam>) {
        for (team in this.teams()) {
            consumer.accept(team)
        }
    }

    @JvmStatic
    public fun Team.asPlayerTeam(): PlayerTeam {
        return this as PlayerTeam
    }

    @JvmStatic
    public fun Team.getOnlinePlayers(): List<ServerPlayer> {
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
    public fun Team.getOnlineCount(): Int {
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
    public fun colouredHeadForTeam(team: Team): ItemStack {
        val head = ItemUtils.colouredHeadForFormatting(team.color)
        head.setHoverName(team.name.literal().unitalicise())
        return head
    }

    @JvmStatic
    public fun createRandomTeams(
        server: MinecraftServer,
        collection: Collection<ServerPlayer>,
        teamSize: Int,
        friendlyFire: Boolean,
        collision: Team.CollisionRule
    ) {
        if (collection.size / teamSize > 16) {
            throw IllegalArgumentException("Too many teams")
        }

        val mutable = ArrayList(collection).apply { shuffle() }
        val teams = Iterators.partition(mutable.iterator(), teamSize)

        val colours = TEAM_COLOURS.shuffled()
        for ((i, players) in teams.withIndex()) {
            val colour = colours[i]
            val team = server.scoreboard.addPlayerTeam(colour.getName())
            team.color = colour
            team.displayName = colour.getName().literal().withStyle(colour)
            team.playerPrefix = "[${colour.getName()}] ".literal().withStyle(colour)
            team.isAllowFriendlyFire = friendlyFire
            team.collisionRule = collision
            for (player in players) {
                server.scoreboard.addPlayerToTeam(player.scoreboardName, team)
            }
        }
    }

    @JvmStatic
    public fun Team.addExtension(extension: Extension) {
        (this as ExtensionHolder).addExtension(extension)
    }

    @JvmStatic
    public fun <T: Extension> Team.getExtension(type: Class<T>): T {
        return (this as ExtensionHolder).getExtension(type)
    }

    @JvmStatic
    public fun Team.getExtensions(): Collection<Extension> {
        return (this as ExtensionHolder).getExtensions()
    }
}