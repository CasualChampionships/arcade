/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.nametags.virtual

import eu.pb4.polymer.virtualentity.api.elements.VirtualElement.InteractionHandler
import net.minecraft.network.protocol.game.ServerboundInteractPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3

public class RetargetingInteractionHandler(
    public val owner: Entity
): InteractionHandler {
    override fun attack(player: ServerPlayer) {
        if (this.owner.isAttackable) {
            player.connection.handleInteract(
                ServerboundInteractPacket.createAttackPacket(this.owner, player.isShiftKeyDown)
            )
        } else {
            player.resetAttackStrengthTicker()
        }
    }

    override fun interact(player: ServerPlayer, hand: InteractionHand) {
        PassthroughInteractionHandler.interact(player, hand)
    }

    override fun interactAt(player: ServerPlayer, hand: InteractionHand, pos: Vec3) {
        PassthroughInteractionHandler.interactAt(player, hand, pos)
    }
}