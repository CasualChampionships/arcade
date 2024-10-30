package net.casual.arcade.minigame.events

import net.casual.arcade.minigame.Minigame
import net.minecraft.server.level.ServerPlayer

/**
 * This event is fired when a player is set as playing,
 * but also when the re-join a [Minigame] already as playing.
 */
public data class MinigameLoadPlayingEvent(
    override val minigame: Minigame,
    val player: ServerPlayer
): MinigameEvent