package net.casual.arcade.events.player

import net.casual.arcade.events.core.CancellableEvent
import net.minecraft.server.level.ServerPlayer

data class PlayerVoidDamageEvent(
    override val player: ServerPlayer
): CancellableEvent.Default(), PlayerEvent