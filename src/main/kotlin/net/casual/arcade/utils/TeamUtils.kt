package net.casual.arcade.utils

import com.google.common.collect.Iterators
import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue.Consumer
import net.casual.arcade.Arcade
import net.casual.arcade.extensions.Extension
import net.casual.arcade.extensions.ExtensionHolder
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.ComponentUtils.prettyName
import net.casual.arcade.utils.ComponentUtils.unitalicise
import net.casual.arcade.utils.ExtensionUtils.addExtension
import net.casual.arcade.utils.ExtensionUtils.getExtension
import net.casual.arcade.utils.ExtensionUtils.getExtensions
import net.minecraft.ChatFormatting
import net.minecraft.ChatFormatting.*
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Scoreboard
import net.minecraft.world.scores.Team

public object TeamUtils {
    public val TEAM_COLOURS: Set<ChatFormatting>
    public val TEAM_COLOURS_NO_GREY: Set<ChatFormatting>

    private val ANIMALS = HashMap<ChatFormatting, List<String>>()

    init {
        TEAM_COLOURS = ChatFormatting.values().copyOfRange(0, 16).toSet()
        TEAM_COLOURS_NO_GREY = TEAM_COLOURS.toMutableSet().apply {
            removeAll(listOf(BLACK, GRAY, DARK_GRAY, WHITE))
        }

        this.addAnimals()
    }

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
    public fun Iterable<PlayerTeam>.toComponent(): MutableComponent {
        val component = Component.empty()
        for (team in this) {
            if (component.siblings.isNotEmpty()) {
                component.append(", ")
            }
            component.append(team.formattedDisplayName)
        }
        return component
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
    ): Collection<PlayerTeam>? {
        val mutable = ArrayList(collection).apply { shuffle() }
        val teams = Iterators.partition(mutable.iterator(), teamSize)

        val generated = ArrayList<PlayerTeam>()
        val colours = TEAM_COLOURS.toMutableSet()
        for (players in teams) {
            var team: PlayerTeam? = null
            var i = 0
            while (team == null) {
                team = getUnusedRandomTeam(server.scoreboard, colours)
                if (i++ > 20) {
                    return null
                }
            }
            colours.remove(team.color)
            team.isAllowFriendlyFire = friendlyFire
            team.collisionRule = collision
            for (player in players) {
                server.scoreboard.addPlayerToTeam(player.scoreboardName, team)
            }
            generated.add(team)
        }
        return generated
    }

    @JvmStatic
    public fun getUnusedRandomTeam(scoreboard: Scoreboard, formatting: Collection<ChatFormatting>): PlayerTeam? {
        val colours = formatting.shuffled()
        if (!TEAM_COLOURS.containsAll(colours)) {
            throw IllegalArgumentException("Some colours are invalid for a team ${formatting}!")
        }
        for (colour in colours) {
            for (animal in ANIMALS[colour]!!.shuffled()) {
                val teamName = "${colour.prettyName()}$animal"
                val team = scoreboard.getPlayerTeam(teamName) ?: scoreboard.addPlayerTeam(teamName)
                if (team.players.isEmpty()) {
                    team.color = colour
                    team.displayName = "${colour.prettyName()} $animal".literal().withStyle(colour)
                    team.playerPrefix = team.formattedDisplayName.append(" ")
                    return team
                }
            }
        }
        return null
    }

    @JvmStatic
    public fun deleteAllRandomTeams(scoreboard: Scoreboard) {
        for (colour in TEAM_COLOURS) {
            for (animal in ANIMALS[colour]!!) {
                val teamName = "${colour.prettyName()}$animal"
                val team = scoreboard.getPlayerTeam(teamName) ?: continue
                scoreboard.removePlayerTeam(team)
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

    private fun addAnimals() {
        ANIMALS[BLACK] = listOf("Bats", "Bears", "Buffaloes")
        ANIMALS[DARK_BLUE] = listOf("Narwhals")
        ANIMALS[DARK_GREEN] = listOf("Gorillas", "Geese", "Geckos")
        ANIMALS[DARK_AQUA] = listOf("Turkeys", "Turtles", "Tigers")
        ANIMALS[DARK_RED] = listOf("Rhinos", "Rabbits", "Robins")
        ANIMALS[DARK_PURPLE] = listOf("Pandas", "Penguins")
        ANIMALS[GOLD] = listOf("Ocelots", "Owls")
        ANIMALS[GRAY] = listOf("Spiders", "Sharks")
        ANIMALS[DARK_GRAY] = listOf("Goats")
        ANIMALS[BLUE] = listOf("Beavers", "Butterflies", "Beetles")
        ANIMALS[GREEN] = listOf("Lizards", "Leopards")
        ANIMALS[AQUA] = listOf("Armadillos", "Axolotls")
        ANIMALS[RED] = listOf("Crocodiles", "Cats")
        ANIMALS[LIGHT_PURPLE] = listOf("Parrots", "Peacocks")
        ANIMALS[YELLOW] = listOf("Yaks")
        ANIMALS[WHITE] = listOf("Whales", "Wolves")
    }
}