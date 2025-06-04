package net.casual.arcade.events.server.player

import net.casual.arcade.events.server.level.LevelEvent
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.state.BlockState

public data class PlayerBlockDropEvent(
    override val player: ServerPlayer,
    override val level: ServerLevel,
    val state: BlockState,
    val pos: BlockPos,
    val tool: ItemStack,
    var drops: List<ItemStack>
): PlayerEvent, LevelEvent
