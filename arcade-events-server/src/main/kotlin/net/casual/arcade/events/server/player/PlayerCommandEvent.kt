package net.casual.arcade.events.server.player

import net.casual.arcade.events.common.CancellableEvent
import net.minecraft.server.level.ServerPlayer

public data class PlayerCommandEvent(
    override val player: ServerPlayer,
    val command: String
): CancellableEvent.Default(), PlayerEvent