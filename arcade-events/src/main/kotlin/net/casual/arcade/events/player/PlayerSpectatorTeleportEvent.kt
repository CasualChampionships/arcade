package net.casual.arcade.events.player

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import java.util.UUID

public data class PlayerSpectatorTeleportEvent(
    override val player: ServerPlayer,
    val targetUUID: UUID
): PlayerEvent {
    public fun getTarget(): Entity? {
        for (level in this.player.server.allLevels) {
            val entity = level.getEntity(this.targetUUID)
            if (entity != null) {
                return entity
            }
        }
        return null
    }
}