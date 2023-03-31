package net.casualuhc.arcade.events.player

import net.casualuhc.arcade.events.core.CancellableEvent
import net.minecraft.server.level.ServerPlayer

class PlayerBlockMinedEvent(
    val player: ServerPlayer
): CancellableEvent()