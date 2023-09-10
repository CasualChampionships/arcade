package net.casual.arcade.utils.minigame.lobby

import net.casual.arcade.minigame.MinigamePhase

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