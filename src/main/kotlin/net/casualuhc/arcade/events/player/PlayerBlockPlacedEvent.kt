package net.casualuhc.arcade.events.player

import net.casualuhc.arcade.events.core.CancellableEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.block.state.BlockState

class PlayerBlockPlacedEvent(
    val player: ServerPlayer,
    val state: BlockState,
    val context: BlockPlaceContext
): CancellableEvent() {
    public override fun cancel() {
        super.cancel()
    }
}