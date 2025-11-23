/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.player

import net.casual.arcade.events.common.CancellableEvent
import net.minecraft.server.level.ServerPlayer

/**
 * Event for when a player drops their items.
 *
 * This is only when the player drops their items
 * from *outside* the gui, dropping items inside any
 * guis doesn't fire this event.
 */
public data class PlayerDropItemEvent(
    override val player: ServerPlayer,
    val all: Boolean
): CancellableEvent.Default(), PlayerEvent
