package net.casual.arcade.events.player

import net.casual.arcade.events.core.CancellableEvent
import net.minecraft.server.level.ServerPlayer

/**
 * Supports PRE, POST.
 */
public data class PlayerJumpEvent(
    override val player: ServerPlayer
): CancellableEvent.Default(), PlayerEvent