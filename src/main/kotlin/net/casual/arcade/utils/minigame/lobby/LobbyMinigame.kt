package net.casual.arcade.utils.minigame.lobby

import com.google.gson.JsonObject
import net.casual.arcade.events.minigame.MinigameAddPlayerEvent
import net.casual.arcade.events.player.PlayerTickEvent
import net.casual.arcade.minigame.Minigame
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel

class LobbyMinigame(
    id: ResourceLocation,
    server: MinecraftServer,
    private val lobby: Lobby,
    private val next: Minigame<*>
): Minigame<LobbyMinigame>(id, server) {
    init {
        this.initialise()
    }

    internal fun countdown() {
        val countdown = this.lobby.getCountdown()
        countdown.countdown(this).then(this::startNextMinigame)
    }

    override fun start() {
        this.setPhase(LobbyMinigamePhases.Waiting)
        this.lobby.area.place()
    }

    override fun initialise() {
        super.initialise()
        this.events.register<MinigameAddPlayerEvent> { this.onMinigameAddPlayer(it) }
        this.events.register<PlayerTickEvent> { this.onPlayerTick(it) }
    }

    override fun getPhases(): List<LobbyMinigamePhases> {
        return LobbyMinigamePhases.values().toList()
    }

    override fun getLevels(): Collection<ServerLevel> {
        return listOf(this.lobby.spawn.level)
    }

    override fun appendAdditionalDebugInfo(json: JsonObject) {
        super.appendAdditionalDebugInfo(json)
        json.add("next_minigame", this.next.getDebugInfo())
    }

    private fun onMinigameAddPlayer(event: MinigameAddPlayerEvent) {
        this.lobby.tryTeleportToSpawn(event.player)
    }

    private fun onPlayerTick(event: PlayerTickEvent) {
        this.lobby.tryTeleportToSpawn(event.player)
    }

    private fun startNextMinigame() {
        for (player in this.getPlayers()) {
            this.next.addPlayer(player)
        }
        this.next.start()
    }
}