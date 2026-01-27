/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.nametags.virtual

import net.casual.arcade.virtual.entity.VirtualEntity
import net.casual.arcade.virtual.entity.interaction.EntityInteraction
import net.minecraft.network.protocol.game.ServerboundInteractPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity

public class RetargetingInteractionHandler(
    private val entity: Entity
): VirtualEntity.InteractionHandler {
    override fun interact(player: ServerPlayer, interaction: EntityInteraction) {
        when (interaction) {
            EntityInteraction.Attack -> this.attack(player)
            else -> PassthroughInteractionHandler.interact(player, interaction)
        }
    }

    private fun attack(player: ServerPlayer) {
        if (this.entity.isAttackable) {
            val packet = ServerboundInteractPacket.createAttackPacket(this.entity, player.isShiftKeyDown)
            player.connection.handleInteract(packet)
            return
        }

        player.resetAttackStrengthTicker()
    }
}