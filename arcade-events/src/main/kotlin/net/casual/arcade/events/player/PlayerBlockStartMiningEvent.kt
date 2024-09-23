package net.casual.arcade.events.player

import net.casual.arcade.events.core.CancellableEvent
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.block.state.BlockState

public data class PlayerBlockStartMiningEvent(
    override val player: ServerPlayer,
    val pos: BlockPos,
    val face: Direction
): CancellableEvent.Default(), PlayerEvent {
    val block: BlockState
        get() = this.level.getBlockState(this.pos)
}