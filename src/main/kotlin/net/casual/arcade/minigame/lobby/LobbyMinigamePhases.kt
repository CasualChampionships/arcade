package net.casual.arcade.minigame.lobby

import net.casual.arcade.minigame.MinigamePhase
import org.jetbrains.annotations.ApiStatus.Experimental

@Experimental
enum class LobbyMinigamePhases(
    override val id: String
): MinigamePhase<LobbyMinigame> {
    Waiting("waiting"),
    Countdown("countdown") {
        override fun start(minigame: LobbyMinigame) {
            minigame.countdown()
        }
    }
}