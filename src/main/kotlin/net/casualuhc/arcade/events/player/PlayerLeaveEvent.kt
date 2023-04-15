package net.casualuhc.arcade.events.player

import net.casualuhc.arcade.events.core.Event
import net.minecraft.server.level.ServerPlayer

data class PlayerLeaveEvent(
    val player: ServerPlayer
): Event()