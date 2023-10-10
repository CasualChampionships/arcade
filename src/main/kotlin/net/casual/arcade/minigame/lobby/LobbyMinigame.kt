package net.casual.arcade.minigame.lobby

import com.google.gson.JsonObject
import net.casual.arcade.events.minigame.MinigameAddNewPlayerEvent
import net.casual.arcade.events.player.PlayerTickEvent
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.MinigamePhase
import net.casual.arcade.utils.MinigameUtils.countdown
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import org.jetbrains.annotations.ApiStatus.Experimental

public abstract class LobbyMinigame(
    server: MinecraftServer,
    private val next: Minigame<*>,
): Minigame<LobbyMinigame>(server) {
    protected abstract val lobby: Lobby

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
        json.add("next_minigame", this.next.getDebugInfo())
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
                minigame.lobby.getCountdown().countdown(minigame).then {
                    minigame.setPhase(MinigamePhase.end())
                }
                minigame.onStartCountdown()
            }

            override fun end(minigame: LobbyMinigame) {
                for (player in minigame.getPlayers()) {
                    minigame.next.addPlayer(player)
                }
                minigame.next.start()
            }
        }
    }
}