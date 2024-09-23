package net.casual.arcade.events.player

import net.casual.arcade.events.core.CancellableEvent
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState

public data class PlayerBlockMinedEvent(
    override val player: ServerPlayer,
    val pos: BlockPos,
    val state: BlockState,
    val entity: BlockEntity?
): CancellableEvent.Default(), PlayerEvent