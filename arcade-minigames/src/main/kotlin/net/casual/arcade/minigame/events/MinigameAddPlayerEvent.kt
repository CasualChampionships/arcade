package net.casual.arcade.minigame.events

import net.casual.arcade.minigame.Minigame
import net.minecraft.server.level.ServerPlayer

/**
 * This event is fired when a player, either
 * new or existing is added to a minigame.
 */
public data class MinigameAddPlayerEvent(
    override val minigame: Minigame,
    val player: ServerPlayer,
    var spectating: Boolean?,
    var admin: Boolean?
): MinigameEvent