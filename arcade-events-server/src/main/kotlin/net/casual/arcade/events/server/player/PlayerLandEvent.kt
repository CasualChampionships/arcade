/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.player

import net.casual.arcade.events.common.CancellableEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.damagesource.DamageSource

public data class PlayerLandEvent(
    override val player: ServerPlayer,
    var damage: Int,
    val distance: Float,
    val multiplier: Float,
    val source: DamageSource
): CancellableEvent.Default(), PlayerEvent