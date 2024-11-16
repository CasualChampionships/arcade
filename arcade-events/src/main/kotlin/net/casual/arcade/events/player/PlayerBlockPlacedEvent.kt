package net.casual.arcade.events.player

import net.casual.arcade.events.BuiltInEventPhases
import net.casual.arcade.events.core.CancellableEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.block.state.BlockState

public data class PlayerBlockPlacedEvent(
    override val player: ServerPlayer,
    val item: BlockItem,
    val state: BlockState,
    val context: BlockPlaceContext
): CancellableEvent.Default(), PlayerEvent {
    public companion object {
        public const val PHASE_PRE: String = BuiltInEventPhases.PRE
        public const val PHASE_POST: String = BuiltInEventPhases.POST
    }
}