package net.casual.arcade.events.server.player

import net.casual.arcade.events.common.CancellableEvent
import net.casual.arcade.events.server.level.LocatedLevelEvent
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.block.state.BlockState

public data class PlayerBlockStartMiningEvent(
    override val player: ServerPlayer,
    override val pos: BlockPos,
    val face: Direction
): CancellableEvent.Default(), PlayerEvent, LocatedLevelEvent {
    override val level: ServerLevel
        get() = this.player.serverLevel()

    val block: BlockState
        get() = this.level.getBlockState(this.pos)
}