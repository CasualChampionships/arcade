package net.casual.arcade.minigame.events

import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.lobby.LobbyMinigame
import net.casual.arcade.utils.time.MinecraftTimeDuration

public data class LobbyMoveToNextMinigameEvent(
    override val minigame: LobbyMinigame,
    public val next: Minigame
): MinigameEvent {
    var delay: MinecraftTimeDuration = MinecraftTimeDuration.ZERO
}