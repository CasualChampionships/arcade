/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.player

import net.casual.arcade.events.common.CancellableEvent
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