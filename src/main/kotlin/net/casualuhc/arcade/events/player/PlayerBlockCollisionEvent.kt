package net.casualuhc.arcade.events.player

import net.casualuhc.arcade.events.core.CancellableEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.block.state.BlockState

data class PlayerBlockCollisionEvent(
    override val player: ServerPlayer,
    val state: BlockState
): CancellableEvent.Default(), PlayerEvent