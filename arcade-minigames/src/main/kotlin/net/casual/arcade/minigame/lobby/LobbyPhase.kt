package net.casual.arcade.minigame.lobby

import net.casual.arcade.minigame.phase.Phase
import net.casual.arcade.minigame.utils.MinigameUtils.countdown
import net.casual.arcade.utils.ArcadeUtils
import net.minecraft.world.level.GameType
import net.minecraft.world.scores.Team

public enum class LobbyPhase(override val id: String): Phase<LobbyMinigame> {
    Waiting("waiting") {
        override fun initialize(minigame: LobbyMinigame) {
            minigame.ui.addBossbar(minigame.getBossbar())
        }

        override fun start(minigame: LobbyMinigame, previous: Phase<LobbyMinigame>) {
            for (player in minigame.players.nonAdmins) {
                player.setGameMode(GameType.ADVENTURE)
            }
            for (team in minigame.teams.getAllTeams()) {
                team.collisionRule = Team.CollisionRule.NEVER
            }
        }
    },
    Readying("readying"),
    Countdown("countdown") {
        override fun initialize(minigame: LobbyMinigame) {
            minigame.ui.removeBossbar(minigame.getBossbar())
        }

        override fun start(minigame: LobbyMinigame, previous: Phase<LobbyMinigame>) {
            val next = minigame.next
            if (next == null) {
                ArcadeUtils.logger.warn("Tried counting down in lobby when there is no next minigame!")
                minigame.setPhase(Waiting)
                return
            }

            minigame.ui.countdown.countdown(minigame).then {
                minigame.setPhase(Phase.end())
            }
            for (team in minigame.teams.getAllTeams()) {
                team.collisionRule = Team.CollisionRule.ALWAYS
            }
        }

        override fun end(minigame: LobbyMinigame, next: Phase<LobbyMinigame>) {
            if (next > this) {
                minigame.moveToNextMinigame()
            }
        }
    }
}