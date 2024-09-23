package net.casual.arcade.events.player

import net.casual.arcade.events.core.CancellableEvent
import net.minecraft.server.level.ServerPlayer

public data class PlayerCommandEvent(
    override val player: ServerPlayer,
    val command: String
): CancellableEvent.Default(), PlayerEvent