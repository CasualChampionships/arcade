package net.casual.arcade.minigame.lobby

import com.google.gson.JsonObject
import net.casual.arcade.Arcade
import net.casual.arcade.events.minigame.MinigameAddNewPlayerEvent
import net.casual.arcade.events.player.PlayerTickEvent
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.MinigamePhase
import net.casual.arcade.utils.MinigameUtils.countdown
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