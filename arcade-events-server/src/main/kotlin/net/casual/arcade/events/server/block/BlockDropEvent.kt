package net.casual.arcade.events.server.block

import net.casual.arcade.events.server.level.LevelEvent
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.loot.LootParams

public data class BlockDropEvent(
    val state: BlockState,
    val params: LootParams.Builder,
    var drops: List<ItemStack>
): LevelEvent {
    override val level: ServerLevel
        get() = this.params.level
}