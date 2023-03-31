package net.casualuhc.arcade.events.player

import net.casualuhc.arcade.events.core.CancellableEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.block.state.BlockState

class PlayerBlockCollisionEvent(
    val entity: ServerPlayer,
    val state: BlockState
): CancellableEvent() {
    public override fun cancel() {
        super.cancel()
    }
}