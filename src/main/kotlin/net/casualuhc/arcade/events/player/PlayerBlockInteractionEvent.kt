package net.casualuhc.arcade.events.player

import net.casualuhc.arcade.events.core.CancellableEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.phys.BlockHitResult

class PlayerBlockInteractionEvent(
    val player: ServerPlayer,
    val level: Level,
    val stack: ItemStack,
    val hand: InteractionHand,
    val result: BlockHitResult
): CancellableEvent.Typed<InteractionResult>()