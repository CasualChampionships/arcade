package net.casualuhc.arcade.events.player

import net.casualuhc.arcade.events.core.CancellableEvent
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState

class PlayerBlockMinedEvent(
    val player: ServerPlayer,
    val pos: BlockPos,
    val state: BlockState,
    val entity: BlockEntity?
): CancellableEvent() {
    public override fun cancel() {
        super.cancel()
    }
}