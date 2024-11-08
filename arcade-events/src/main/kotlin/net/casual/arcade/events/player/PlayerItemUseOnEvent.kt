package net.casual.arcade.events.player

import net.casual.arcade.events.core.CancellableEvent
import net.casual.arcade.events.level.LocatedLevelEvent
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.UseOnContext

public data class PlayerItemUseOnEvent(
    override val player: ServerPlayer,
    val stack: ItemStack,
    val context: UseOnContext
): CancellableEvent.Typed<InteractionResult>(), PlayerEvent, LocatedLevelEvent {
    override val pos: BlockPos
        get() = this.context.clickedPos
    override val level: ServerLevel
        get() = this.context.level as ServerLevel

}