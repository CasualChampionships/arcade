/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.resources.event

import net.casual.arcade.events.server.player.PlayerEvent
import net.casual.arcade.resources.pack.PackState
import net.minecraft.server.level.ServerPlayer

public data class PlayerPacksSuccessEvent(
    override val player: ServerPlayer,
    val packs: Collection<PackState>
): PlayerEvent