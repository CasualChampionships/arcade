package net.casual.arcade.events.server.player

import net.casual.arcade.events.common.CancellableEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.block.state.BlockState

public data class PlayerBlockCollisionEvent(
    override val player: ServerPlayer,
    val state: BlockState
): CancellableEvent.Default(), PlayerEvent