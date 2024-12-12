package net.casual.arcade.events.server.player

import net.minecraft.server.level.ServerPlayer

public data class PlayerFallEvent(
    override val player: ServerPlayer,
    val distance: Double,
    val onGround: Boolean
): PlayerEvent