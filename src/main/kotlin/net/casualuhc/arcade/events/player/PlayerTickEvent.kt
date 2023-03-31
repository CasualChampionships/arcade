package net.casualuhc.arcade.events.player

import net.casualuhc.arcade.events.core.Event
import net.minecraft.server.level.ServerPlayer

class PlayerTickEvent(
    val player: ServerPlayer
): Event()