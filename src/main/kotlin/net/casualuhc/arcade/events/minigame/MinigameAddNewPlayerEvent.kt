package net.casualuhc.arcade.events.minigame

import net.casualuhc.arcade.events.core.CancellableEvent
import net.casualuhc.arcade.minigame.Minigame
import net.minecraft.server.level.ServerPlayer

/**
 * This event is fired when a new player
 * joins the minigame.
 */
data class MinigameAddNewPlayerEvent(
    override val minigame: Minigame,
    val player: ServerPlayer
): CancellableEvent.Default(), MinigameEvent