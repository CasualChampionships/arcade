package net.casual.arcade.utils.minigame.lobby

import net.casual.arcade.area.PlaceableArea
import net.casual.arcade.utils.impl.Location
import net.casual.arcade.utils.minigame.MinigameCountdown
import net.casual.arcade.utils.PlayerUtils.teleportTo
import net.minecraft.server.level.ServerPlayer

interface Lobby {
    val area: PlaceableArea
    val spawn: Location

    fun getCountdown(): MinigameCountdown

    fun getSpawn(player: ServerPlayer): Location {
        return this.spawn
    }

    fun forceTeleportToSpawn(player: ServerPlayer) {
        player.teleportTo(this.getSpawn(player))
    }

    fun tryTeleportToSpawn(player: ServerPlayer): Boolean {
        if (player.level() != this.spawn.level || !this.area.getEntityBoundingBox().contains(player.position())) {
            this.forceTeleportToSpawn(player)
            return true
        }
        return false
    }
}