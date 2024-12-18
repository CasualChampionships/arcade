/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.player

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerPlayer

public data class PlayerSleepEvent(
    override val player: ServerPlayer,
    val bedPosition: BlockPos
): PlayerEvent