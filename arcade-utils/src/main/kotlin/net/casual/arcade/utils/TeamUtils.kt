package net.casual.arcade.utils

import com.google.common.collect.Iterators
import com.google.common.collect.LinkedHashMultimap
import com.google.common.collect.Multimap
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import net.casual.arcade.util.ducks.OverridableColor
import net.casual.arcade.utils.ComponentUtils.joinToComponent
import net.casual.arcade.utils.ComponentUtils.prettyName
import net.casual.arcade.utils.ItemUtils.named
import net.casual.arcade.utils.PlayerUtils.player
import net.minecraft.ChatFormatting
import net.minecraft.ChatFormatting.*
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.server.MinecraftServer
import net.minecraft.server.ServerScoreboard
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Scoreboard
import net.minecraft.world.scores.Team

public object TeamUtils {
    @Suppress("JoinDeclarationAndAssignment")
    public val TEAM_COLOURS: Set<ChatFormatting>
    public val TEAM_COLOURS_NO_GREY: Set<ChatFormatting>

    private val ANIMALS = HashMap<ChatFormatting, List<String>>()

    init {
        TEAM_COLOURS = entries.slice(0..< 16).toSet()
        TEAM_COLOURS_NO_GREY = TEAM_COLOURS.toMutableSet().apply {
            removeAll(setOf(BLACK, GRAY, DARK_GRAY, WHITE))
        }

        this.addAnimals()
    }

    @JvmStatic
    @Deprecated(
        "Use the vanilla method",
        ReplaceWith("this.scoreboard.playerTeams")
    )
    public fun MinecraftServer.teams(): Collection<PlayerTeam> {
        return this.scoreboard.playerTeams
    }

    @JvmStatic
    public fun getTeamsFor(entities: Iterable<Entity>): MutableSet<PlayerTeam> {
        val teams = ReferenceOpenHashSet<PlayerTeam>()
        for (entity in entities) {
            val team = entity.team ?: continue
            teams.add(team)
        }
        return teams
    }

    @JvmStatic
    public fun getMappedTeamsFor(entities: Iterable<Entity>): Multimap<PlayerTeam, Entity> {
        val teams = LinkedHashMultimap.create<PlayerTeam, Entity>()
        for (entity in entities) {
            val team = entity.team ?: continue
            teams.put(team, entity)
        }
        return teams
    }

    @JvmStatic
    public fun ServerScoreboard.getOrCreateTeam(name: String, modifier: PlayerTeam.() -> Unit = {}): PlayerTeam {
        val team = this.getPlayerTeam(name) ?: this.addPlayerTeam(name)
        team.modifier()
        return team
    }

    @JvmStatic
    public fun Team.asPlayerTeam(): PlayerTeam {
        return this as PlayerTeam
    }

    @JvmStatic
    public fun Team.getOnlinePlayers(server: MinecraftServer = ServerUtils.getServer()): List<ServerPlayer> {
        val team = ArrayList<ServerPlayer>()
        for (name in this.players) {
            val player = server.player(name)
            if (player != null) {
                team.add(player)
            }
        }
        return team
    }

    @JvmStatic
    public fun Team.getOnlineCount(server: MinecraftServer = ServerUtils.getServer()): Int {
        var count = 0
        for (name in this.players) {
            val player = server.player(name)
            if (player != null) {
                count++
            }
        }
        return count
    }

    @JvmStatic
    public fun PlayerTeam.setHexColor(color: Int?) {
        (this as OverridableColor).`arcade$setColor`(color)
    }

    @JvmStatic
    public fun PlayerTeam.getHexColor(): Int? {
        return (this as OverridableColor).`arcade$getColor`() ?: this.color.color
    }

    @JvmStatic
    public fun MutableComponent.color(team: PlayerTeam?): MutableComponent {
        if (team == null) {
            return this
        }
        val color = team.getHexColor()
        if (color != null) {
            this.withColor(color)
        }
        return this
    }

    @JvmStatic
    @Deprecated(
        "Use joinToComponent instead",
        ReplaceWith("this.joinToComponent { it.formattedDisplayName }")
    )
    public fun Iterable<PlayerTeam>.toComponent(): MutableComponent {
        return this.joinToComponent { it.formattedDisplayName }
    }

    @JvmStatic
    public fun colouredHeadForTeam(team: Team): ItemStack {
        val head = ItemUtils.colouredHeadForFormatting(team.color)
        head.named(team.name)
        return head
    }

    @JvmStatic
    public fun createRandomTeams(
        server: MinecraftServer,
        collection: Collection<Entity>,
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
                    team.displayName = Component.literal("${colour.prettyName()} $animal").withStyle(colour)
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