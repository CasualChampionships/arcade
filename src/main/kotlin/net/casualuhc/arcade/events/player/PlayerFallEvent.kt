package net.casualuhc.arcade.events.player

import net.minecraft.server.level.ServerPlayer

data class PlayerFallEvent(
    override val player: ServerPlayer,
    val distance: Double,
    val onGround: Boolean
): PlayerEvent