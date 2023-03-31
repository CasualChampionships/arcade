package net.casualuhc.arcade.events.player

import net.casualuhc.arcade.events.core.CancellableEvent
import net.minecraft.server.level.ServerPlayer

class PlayerVoidEvent(
    val player: ServerPlayer
): CancellableEvent() {
    public override fun cancel() {
        super.cancel()
    }
}