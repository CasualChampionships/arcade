package net.casual.arcade.minigame.events.lobby

import com.google.gson.JsonObject
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.casual.arcade.Arcade
import net.casual.arcade.commands.arguments.EnumArgument
import net.casual.arcade.commands.arguments.MinigameArgument
import net.casual.arcade.events.minigame.LobbyMoveToNextMinigameEvent
import net.casual.arcade.events.minigame.MinigameAddNewPlayerEvent
import net.casual.arcade.events.server.ServerTickEvent
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.phase.Phase
import net.casual.arcade.minigame.serialization.MinigameCreationContext
import net.casual.arcade.scheduler.MinecraftTimeUnit
import net.casual.arcade.utils.CommandUtils.commandSuccess
import net.casual.arcade.utils.CommandUtils.fail
import net.casual.arcade.utils.CommandUtils.success
import net.casual.arcade.utils.ComponentUtils.command
import net.casual.arcade.utils.ComponentUtils.green
import net.casual.arcade.utils.ComponentUtils.lime
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.ComponentUtils.singleUseFunction
import net.casual.arcade.utils.EventUtils.broadcast
import net.casual.arcade.utils.GameRuleUtils.resetToDefault
import net.casual.arcade.utils.GameRuleUtils.set
import net.casual.arcade.utils.MinigameUtils.countdown
import net.casual.arcade.utils.MinigameUtils.requiresAdminOrPermission
import net.casual.arcade.utils.MinigameUtils.transferAdminAndSpectatorTeamsTo
import net.casual.arcade.utils.MinigameUtils.transferPlayersTo
import net.casual.arcade.utils.PlayerUtils.clearPlayerInventory
import net.casual.arcade.utils.PlayerUtils.resetExperience
import net.casual.arcade.utils.PlayerUtils.resetHealth
import net.casual.arcade.utils.PlayerUtils.resetHunger
import net.casual.arcade.utils.PlayerUtils.toComponent
import net.casual.arcade.utils.TeamUtils.toComponent
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.GameRules
import net.minecraft.world.level.GameType
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Team

public open class LobbyMinigame(
    server: MinecraftServer,
    public val lobby: Lobby,
): Minigame<LobbyMinigame>(server) {
    private var awaiting: (() -> Component)? = null
    private var next: Minigame<*>? = null

    private val bossbar = this.lobby.createBossbar().apply { then(::completeBossBar) }

    override val id: ResourceLocation = ID

    override fun initialize() {
        super.initialize()
        this.levels.add(this.lobby.spawn.level)

        this.setGameRules {
            resetToDefault()
            set(GameRules.RULE_DOINSOMNIA, false)
            set(GameRules.RULE_DOFIRETICK, false)
            set(GameRules.RULE_DOMOBSPAWNING, false)
            set(GameRules.RULE_DAYLIGHT, false)
            set(GameRules.RULE_FALL_DAMAGE, false)
            set(GameRules.RULE_DROWNING_DAMAGE, false)
            set(GameRules.RULE_DOENTITYDROPS, false)
            set(GameRules.RULE_WEATHER_CYCLE, false)
            set(GameRules.RULE_DO_TRADER_SPAWNING, false)
            set(GameRules.RULE_DOBLOCKDROPS, false)
            set(GameRules.RULE_SNOW_ACCUMULATION_HEIGHT, 0)
            set(GameRules.RULE_RANDOMTICKING, 0)
        }
        this.events.register<MinigameAddNewPlayerEvent> { (_, player) ->
            this.lobby.forceTeleportToSpawn(player)
            player.resetHealth()
            player.resetExperience()
            player.resetHunger()
            player.clearPlayerInventory()
        }
        this.events.register<ServerTickEvent> {
            for (player in this.getNonAdminPlayers()) {
                this.lobby.tryTeleportToSpawn(player)
            }
        }

        this.commands.register(this.createLobbyCommand())

        this.settings.canPvp.set(false)
        this.settings.canGetHungry.set(false)
        this.settings.canBreakBlocks.set(false)
        this.settings.canPlaceBlocks.set(false)
        this.settings.canDropItems.set(false)
        this.settings.canPickupItems.set(false)
        this.settings.canTakeDamage.set(false)
        this.settings.canAttackEntities.set(false)
        this.settings.canInteractAll = false

        this.lobby.area.replace()
    }

    public open fun getTeamsToReady(): Collection<PlayerTeam> {
        return this.teams.getPlayingTeams()
    }

    public open fun getPlayersToReady(): Collection<ServerPlayer> {
        return this.getPlayingPlayers()
    }

    public fun getNextMinigame(): Minigame<*>? {
        return this.next
    }

    public fun setNextMinigame(minigame: Minigame<*>) {
        this.next = minigame
    }

    private fun onReady() {
        this.awaiting = null
        val component = "All players are ready, click to start!".literal().green().singleUseFunction {
            this.setPhase(LobbyPhase.Countdown)
        }
        this.chat.broadcastTo(component, this.getAdminPlayers())
    }

    private fun moveToNextMinigame() {
        val next = this.next!!
        if (next.closed) {
            Arcade.logger.warn("Failed to move to next minigame ${next.id}, it was closed before starting!")
            this.next = null
            return
        }

        LobbyMoveToNextMinigameEvent(this, next).broadcast()

        this.transferAdminAndSpectatorTeamsTo(next)
        this.transferPlayersTo(next)
        next.start()

        this.setPhase(LobbyPhase.Waiting)
        this.next = null
    }

    final override fun getPhases(): List<LobbyPhase> {
        return LobbyPhase.entries.toList()
    }

    override fun appendAdditionalDebugInfo(json: JsonObject) {
        super.appendAdditionalDebugInfo(json)
        val next = this.next
        if (next != null) {
            json.add("next_minigame", next.getDebugInfo())
        }
    }

    protected open fun createLobbyCommand(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("lobby").requiresAdminOrPermission().then(
            Commands.literal("next").then(
                Commands.literal("settings").executes(this::nextMinigameSettings)
            ).then(
                Commands.literal("set").then(
                    Commands.literal("existing").then(
                        Commands.argument("minigame", MinigameArgument.minigame()).executes(this::setNextMinigame)
                    )
                ).then(
                    Commands.literal("new").then(
                        Commands.argument("minigame", MinigameArgument.Factory.factory()).executes(this::setNextNewMinigame)
                    )
                )
            ).then(
                Commands.literal("unset").executes(this::unsetNextMinigame)
            )
        ).then(
            Commands.literal("place").executes(this::placeLobby)
        ).then(
            Commands.literal("replace").executes(this::replaceLobby)
        ).then(
            Commands.literal("delete").executes(this::deleteLobby)
        ).then(
            Commands.literal("tp").executes(this::teleportToLobby)
        ).then(
            Commands.literal("countdown").executes(this::startCountdown)
        ).then(
            Commands.literal("ready").then(
                Commands.literal("players").executes(this::readyPlayers)
            ).then(
                Commands.literal("teams").executes(this::readyTeams)
            ).then(
                Commands.literal("awaiting").executes(this::awaitingReady)
            )
        ).then(
            Commands.literal("start").then(
                Commands.literal("in").then(
                    Commands.argument("time", IntegerArgumentType.integer(1)).then(
                        Commands.argument("unit", EnumArgument.enumeration(MinecraftTimeUnit::class.java)).executes(this::setTime)
                    )
                )
            )
        )
    }

    private fun nextMinigameSettings(context: CommandContext<CommandSourceStack>): Int {
        val next = this.next ?: throw NO_MINIGAME.create()
        val player = context.source.playerOrException
        player.openMenu(next.settings.menu())
        return this.commandSuccess()
    }

    private fun setNextMinigame(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        this.next = minigame
        return context.source.success("Successfully set the next minigame to ${minigame.id}")
    }

    private fun setNextNewMinigame(context: CommandContext<CommandSourceStack>): Int {
        val factory = MinigameArgument.Factory.getFactory(context, "minigame")
        this.next?.close()
        val next = factory.create(MinigameCreationContext(context.source.server))
        next.tryInitialize()
        this.next = next
        return context.source.success("Successfully set the next minigame to ${next.id}")
    }

    private fun unsetNextMinigame(context: CommandContext<CommandSourceStack>): Int {
        this.next?.close()
        this.next = null
        return context.source.success("Successfully unset the next minigame")
    }

    private fun placeLobby(context: CommandContext<CommandSourceStack>): Int {
        this.lobby.area.place()
        return context.source.success("Successfully placed the lobby")
    }

    private fun replaceLobby(context: CommandContext<CommandSourceStack>): Int {
        this.lobby.area.replace()
        return context.source.success("Successfully replaced the lobby")
    }

    private fun deleteLobby(context: CommandContext<CommandSourceStack>): Int {
        this.lobby.area.removeAllButPlayers()
        return context.source.success("Successfully removed the lobby")
    }

    private fun teleportToLobby(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.playerOrException
        this.lobby.forceTeleportToSpawn(player)
        return context.source.success("Successfully teleported to the lobby")
    }

    private fun startCountdown(context: CommandContext<CommandSourceStack>): Int {
        this.next ?: return context.source.fail("Cannot move to next minigame, it has not been set!")
        this.setPhase(LobbyPhase.Countdown)
        return context.source.success("Successfully started the countdown")
    }

    private fun readyPlayers(context: CommandContext<CommandSourceStack>): Int {
        this.next ?: return context.source.fail("Cannot ready for next minigame, it has not been set!")
        this.setPhase(LobbyPhase.Readying)
        val awaiting = this.ui.readier.arePlayersReady(this.getPlayersToReady(), this::onReady)
        this.awaiting = {
            "Awaiting the following players: ".literal().append(awaiting.toComponent())
        }
        return context.source.success("Successfully broadcasted ready check")
    }

    private fun readyTeams(context: CommandContext<CommandSourceStack>): Int {
        this.next ?: return context.source.fail("Cannot ready for next minigame, it has not been set!")
        this.setPhase(LobbyPhase.Readying)
        val awaiting = this.ui.readier.areTeamsReady(this.getTeamsToReady(), this::onReady)
        this.awaiting = {
            "Awaiting the following teams: ".literal().append(awaiting.toComponent())
        }
        return context.source.success("Successfully broadcasted ready check")
    }

    private fun awaitingReady(context: CommandContext<CommandSourceStack>): Int {
        val awaiting = this.awaiting ?: return context.source.fail("Not currently awaiting any players or teams to be ready")
        return context.source.success(awaiting())
    }

    private fun setTime(context: CommandContext<CommandSourceStack>): Int {
        val time = IntegerArgumentType.getInteger(context, "time")
        val unit = EnumArgument.getEnumeration(context, "unit", MinecraftTimeUnit::class.java)
        val duration = unit.duration(time)
        this.bossbar.setDuration(duration)
        return context.source.success("Countdown will begin in $time ${unit.name}")
    }

    private fun completeBossBar() {
        val message = "Lobby waiting period has finished. ".literal()
        val teams = "[Click to ready teams]".literal().lime().command("/lobby ready teams")
        val players = "[Click to ready players]".literal().lime().command("/lobby ready players")
        val component = message.append(teams).append(" or ").append(players)

        this.chat.broadcastTo(component, this.getAdminPlayers())
    }

    public companion object {
        private val NO_MINIGAME = SimpleCommandExceptionType("Lobby has no next minigame".literal())

        public val ID: ResourceLocation = Arcade.id("lobby")
    }

    public enum class LobbyPhase(override val id: String): Phase<LobbyMinigame> {
        Waiting("waiting") {
            override fun start(minigame: LobbyMinigame) {
                minigame.ui.addBossbar(minigame.bossbar)
                for (player in minigame.getNonAdminPlayers()) {
                    player.setGameMode(GameType.ADVENTURE)
                }
                for (team in minigame.teams.getAllTeams()) {
                    team.collisionRule = Team.CollisionRule.NEVER
                }
            }
        },
        Readying("readying"),
        Countdown("countdown") {
            override fun start(minigame: LobbyMinigame) {
                val next = minigame.next
                if (next == null) {
                    Arcade.logger.warn("Tried counting down in lobby when there is no next minigame!")
                    minigame.setPhase(Waiting)
                    return
                }

                minigame.lobby.getCountdown().countdown(minigame).then {
                    minigame.setPhase(Phase.end())
                }
                minigame.ui.removeBossbar(minigame.bossbar)
                for (team in minigame.teams.getAllTeams()) {
                    team.collisionRule = Team.CollisionRule.ALWAYS
                }
            }

            override fun end(minigame: LobbyMinigame) {
                minigame.moveToNextMinigame()

                minigame.lobby.area.removeAllButPlayers()
            }
        }
    }
}