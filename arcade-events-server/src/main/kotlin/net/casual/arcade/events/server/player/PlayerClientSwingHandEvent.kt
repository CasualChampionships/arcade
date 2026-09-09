/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.player

import net.casual.arcade.events.common.CancellableEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.component.SwingAnimation

public data class PlayerClientSwingHandEvent(
    override val player: ServerPlayer,
    val hand: InteractionHand,
    val animation: SwingAnimation
): CancellableEvent.Simple(), PlayerEvent