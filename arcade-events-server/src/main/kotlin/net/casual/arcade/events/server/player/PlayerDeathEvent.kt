/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.player

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.damagesource.DamageSource

/**
 * Supports PRE, POST.
 */
public data class PlayerDeathEvent(
    override val player: ServerPlayer,
    val source: DamageSource
): PlayerEvent