/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.nametags.virtual

import net.casual.arcade.virtual.entity.VirtualEntity
import net.casual.arcade.virtual.entity.interaction.EntityInteraction
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand

public object PassthroughInteractionHandler: VirtualEntity.InteractionHandler {
    override fun interact(player: ServerPlayer, interaction: EntityInteraction) {
        when (interaction) {
            is EntityInteraction.Use -> this.use(player, interaction.hand)
            else -> { }
        }
    }

    private fun use(player: ServerPlayer, hand: InteractionHand) {
        player.gameMode.useItem(player, player.level(), player.getItemInHand(hand), hand)
    }
}