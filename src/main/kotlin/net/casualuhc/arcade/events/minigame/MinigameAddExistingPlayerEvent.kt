package net.casualuhc.arcade.events.minigame

import net.casualuhc.arcade.minigame.Minigame
import net.minecraft.server.level.ServerPlayer

/**
 * This event is fired when a player who was
 * previously part of a minigame is re-added.
 *
 * For example: when the player relogs during a minigame.
 */
data class MinigameAddExistingPlayerEvent(
    override val minigame: Minigame,
    val player: ServerPlayer
): MinigameEvent