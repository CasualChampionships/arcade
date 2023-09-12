package net.casual.arcade.events.player

import net.casual.arcade.events.core.CancellableEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.block.state.BlockState

public data class PlayerBlockCollisionEvent(
    override val player: ServerPlayer,
    val state: BlockState
): CancellableEvent.Default(), PlayerEvent