package net.casual.arcade.minigame.events

import net.casual.arcade.minigame.Minigame
import net.minecraft.server.level.ServerPlayer

/**
 * This event is fired when a player is set as spectating,
 * but also when the re-join a [Minigame] already as spectating.
 */
public data class MinigameLoadSpectatingEvent(
    override val minigame: Minigame,
    val player: ServerPlayer
): MinigameEvent