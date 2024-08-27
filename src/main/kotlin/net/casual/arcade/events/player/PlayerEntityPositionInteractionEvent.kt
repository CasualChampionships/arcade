package net.casual.arcade.events.player

import net.casual.arcade.events.core.CancellableEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3

/**
 * This is used for interacting at a specific position on an entity,
 * for example, when clicking on armor stands to add/remove armor.
 */
public data class PlayerEntityPositionInteractionEvent(
    override val player: ServerPlayer,
    val target: Entity,
    val hand: InteractionHand,
    val position: Vec3
): CancellableEvent.Typed<InteractionResult>(), PlayerEvent