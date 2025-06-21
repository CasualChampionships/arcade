/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.player

import net.casual.arcade.events.common.CancellableEvent
import net.casual.arcade.utils.PlayerUtils.levelServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import java.util.*

public data class PlayerSpectatorTeleportEvent(
    override val player: ServerPlayer,
    val targetUUID: UUID
): CancellableEvent.Default(), PlayerEvent {
    public fun getTarget(): Entity? {
        for (level in this.player.levelServer.allLevels) {
            val entity = level.getEntity(this.targetUUID)
            if (entity != null) {
                return entity
            }
        }
        return null
    }
}