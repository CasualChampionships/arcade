/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.client.player

import net.casual.arcade.events.common.CancellableEvent
import net.casual.arcade.events.common.ClientSideEvent
import net.minecraft.client.player.LocalPlayer
import net.minecraft.world.entity.Entity

public data class PlayerTryAttackEvent(
    val player: LocalPlayer,
    val target: Entity
): CancellableEvent.Simple(), ClientSideEvent