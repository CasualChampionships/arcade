/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.player

import net.casual.arcade.events.BuiltInEventPhases
import net.casual.arcade.events.common.CancellableEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.ItemStack

public data class PlayerItemUseEvent(
    override val player: ServerPlayer,
    val stack: ItemStack,
    val hand: InteractionHand
): CancellableEvent.Typed<InteractionResult>(), PlayerEvent {
    public companion object {
        /**
         * This phase will trigger the listener early, i.e. before the game
         * has checked whether the player can actually interact with the item.
         */
        public const val PHASE_PRE: String = BuiltInEventPhases.PRE

        /**
         * This phase will trigger the listener if the item *will* be interacted
         * with in the game.
         */
        public const val PHASE_DEFAULT: String = BuiltInEventPhases.DEFAULT
    }
}