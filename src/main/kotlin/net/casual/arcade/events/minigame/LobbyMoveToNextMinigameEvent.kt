package net.casual.arcade.events.minigame

import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.events.lobby.LobbyMinigame

public class LobbyMoveToNextMinigameEvent(
    override val minigame: LobbyMinigame,
    public val next: Minigame<*>
): MinigameEvent