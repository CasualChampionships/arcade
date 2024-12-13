package net.casual.arcade.events.server.player

import net.casual.arcade.events.common.CancellableEvent
import net.minecraft.server.level.ServerPlayer

public data class PlayerVoidDamageEvent(
    override val player: ServerPlayer
): CancellableEvent.Default(), PlayerEvent