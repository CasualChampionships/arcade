/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.nametags.virtual

import eu.pb4.polymer.virtualentity.api.elements.VirtualElement.InteractionHandler
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.phys.Vec3

public object PassthroughInteractionHandler: InteractionHandler {
    override fun interact(player: ServerPlayer, hand: InteractionHand) {
        val item = player.getItemInHand(hand)
        player.gameMode.useItem(player, player.level(), item, hand)
    }

    override fun interactAt(player: ServerPlayer, hand: InteractionHand, pos: Vec3) {
        this.interact(player, hand)
    }
}