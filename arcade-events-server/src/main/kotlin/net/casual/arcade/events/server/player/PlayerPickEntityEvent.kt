/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.player

import net.casual.arcade.events.common.CancellableEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity

public data class PlayerPickEntityEvent(
    override val player: ServerPlayer,
    val entity: Entity
): CancellableEvent.Default(), PlayerEvent