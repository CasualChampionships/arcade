package net.casualuhc.arcade.events.player

import net.casualuhc.arcade.events.core.Event
import net.minecraft.server.level.ServerPlayer

class PlayerFallEvent(
    val player: ServerPlayer,
    val distance: Double,
    val onGround: Boolean
): Event()