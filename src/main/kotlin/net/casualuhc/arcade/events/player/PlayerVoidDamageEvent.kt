package net.casualuhc.arcade.events.player

import net.casualuhc.arcade.events.core.CancellableEvent
import net.minecraft.server.level.ServerPlayer

data class PlayerVoidDamageEvent(
    override val player: ServerPlayer
): CancellableEvent.Default(), PlayerEvent