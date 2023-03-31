package net.casualuhc.arcade.events.player

import net.casualuhc.arcade.events.core.Event
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer

class PlayerDimensionChangeEvent(
    val player: ServerPlayer,
    val destination: ServerLevel
): Event()