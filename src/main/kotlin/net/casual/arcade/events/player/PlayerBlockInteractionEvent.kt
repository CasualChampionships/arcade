package net.casual.arcade.events.player

import net.casual.arcade.events.core.CancellableEvent
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.phys.BlockHitResult

data class PlayerBlockInteractionEvent(
    override val player: ServerPlayer,
    val stack: ItemStack,
    val hand: InteractionHand,
    val result: BlockHitResult
): CancellableEvent.Typed<InteractionResult>(), PlayerEvent