package net.casual.arcade.events.player

import net.casual.arcade.events.core.CancellableEvent
import net.fabricmc.fabric.api.util.TriState
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.block.state.BlockState

/**
 * Supports PRE, POST.
 */
public data class PlayerBlockPlacedEvent(
    override val player: ServerPlayer,
    val item: BlockItem,
    val state: BlockState,
    val context: BlockPlaceContext,
    val successful: TriState
): CancellableEvent.Default(), PlayerEvent