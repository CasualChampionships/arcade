package net.casual.arcade.minigame.events

import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.events.lobby.LobbyMinigame
import net.casual.arcade.scheduler.MinecraftTimeDuration

public data class LobbyMoveToNextMinigameEvent(
    override val minigame: LobbyMinigame,
    public val next: Minigame<*>
): MinigameEvent {
    var delay: MinecraftTimeDuration = MinecraftTimeDuration.ZERO
}