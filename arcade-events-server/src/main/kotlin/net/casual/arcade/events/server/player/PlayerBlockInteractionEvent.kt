/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.player

import net.casual.arcade.events.common.CancellableEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.BlockHitResult

public data class PlayerBlockInteractionEvent(
    override val player: ServerPlayer,
    val stack: ItemStack,
    val hand: InteractionHand,
    val result: BlockHitResult
): CancellableEvent.Typed<InteractionResult>(), PlayerEvent {
    var preventUsingOnBlock: Boolean = false
        private set

    public fun preventUsingOnBlock() {
        this.preventUsingOnBlock = true
    }
}