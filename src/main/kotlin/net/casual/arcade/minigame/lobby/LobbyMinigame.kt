package net.casual.arcade.minigame.lobby

import com.google.gson.JsonObject
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.casual.arcade.Arcade
import net.casual.arcade.commands.arguments.MinigameArgument
import net.casual.arcade.events.minigame.MinigameAddNewPlayerEvent
import net.casual.arcade.events.player.PlayerTickEvent
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.MinigamePhase
import net.casual.arcade.utils.CommandUtils.commandSuccess
import net.casual.arcade.utils.MinigameUtils.countdown
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel

public abstract class LobbyMinigame(
    server: MinecraftServer
): Minigame<LobbyMinigame>(server) {
    protected abstract val lobby: Lobby

    private var next: Minigame<*>? = null

    init {
        this.initialise()
    }

    override fun initialise() {
        super.initialise()
        this.events.register<MinigameAddNewPlayerEvent> { (_, player) ->
            this.lobby.forceTeleportToSpawn(player)
        }
        this.events.register<PlayerTickEvent> { (player) ->
            this.lobby.tryTeleportToSpawn(player)
        }
        this.registerLobbyCommand()
    }

    public open fun onStart() {

    }

    public open fun onStartCountdown() {

    }

    public fun getNextMinigame(): Minigame<*>? {
        return this.next
    }

    public fun setNextMinigame(minigame: Minigame<*>) {
        this.next = minigame
    }

    final override fun start() {
        this.setPhase(Phases.Waiting)
    }

    final override fun getPhases(): List<Phases> {
        return listOf(Phases.Waiting, Phases.Countdown)
    }

    final override fun getLevels(): Collection<ServerLevel> {
        return listOf(this.lobby.spawn.level)
    }

    override fun appendAdditionalDebugInfo(json: JsonObject) {
        super.appendAdditionalDebugInfo(json)
        val next = this.next
        if (next != null) {
            json.add("next_minigame", next.getDebugInfo())
        }
    }

    private fun registerLobbyCommand() {
        this.commands.register(
            Commands.literal("lobby").then(
                Commands.literal("next").then(
                    Commands.literal("settings").executes(this::nextMinigameSettings)
                ).then(
                    Commands.literal("set").then(
                        Commands.argument("minigame", MinigameArgument.minigame()).executes(this::setNextMinigame)
                    )
                ).then(
                    Commands.literal("unset").executes(this::unsetNextMinigame)
                )
            )
        )
    }

    private fun nextMinigameSettings(context: CommandContext<CommandSourceStack>): Int {
        val next = this.next ?: throw NO_MINIGAME.create()
        val player = context.source.playerOrException
        player.openMenu(next.createRulesMenu())
        return this.commandSuccess()
    }

    private fun setNextMinigame(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        this.next = minigame
        return this.commandSuccess()
    }

    private fun unsetNextMinigame(context: CommandContext<CommandSourceStack>): Int {
        this.next = null
        return this.commandSuccess()
    }

    private companion object {
        val NO_MINIGAME = SimpleCommandExceptionType(Component.literal("Lobby has no next minigame"))
    }

    public enum class Phases(override val id: String): MinigamePhase<LobbyMinigame> {
        Waiting("waiting") {
            override fun start(minigame: LobbyMinigame) {
                minigame.lobby.area.place()
                minigame.onStart()
            }
        },
        Countdown("countdown") {
            override fun start(minigame: LobbyMinigame) {
                val next = minigame.next
                if (next == null) {
                    Arcade.logger.warn("Tried counting down in lobby when there is no next minigame!")
                    minigame.setPhase(Waiting)
                    return
                }

                minigame.lobby.getCountdown().countdown(minigame).then {
                    minigame.setPhase(MinigamePhase.end())
                }
                minigame.onStartCountdown()
            }

            override fun end(minigame: LobbyMinigame) {
                val next = minigame.next!!
                for (player in minigame.getPlayers()) {
                    next.addPlayer(player)
                }
                next.start()
            }
        }
    }
}