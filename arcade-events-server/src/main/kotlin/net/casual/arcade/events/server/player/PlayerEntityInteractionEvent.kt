package net.casual.arcade.events.server.player

import net.casual.arcade.events.common.CancellableEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.Entity

public data class PlayerEntityInteractionEvent(
    override val player: ServerPlayer,
    val target: Entity,
    val hand: InteractionHand
): CancellableEvent.Typed<InteractionResult>(), PlayerEvent