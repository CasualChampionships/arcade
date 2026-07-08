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

public data class PlayerEntityInteractionEvent(
    override val player: ServerPlayer,
    val target: Entity,
    val hand: InteractionHand,
    val pos: Vec3
): CancellableEvent.WithResult<InteractionResult>(), PlayerEvent