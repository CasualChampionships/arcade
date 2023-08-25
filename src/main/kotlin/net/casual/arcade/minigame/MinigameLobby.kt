package net.casual.arcade.minigame

import net.casual.arcade.map.PlaceableMap
import net.casual.arcade.math.Location
import net.casual.arcade.utils.PlayerUtils.teleportTo
import net.minecraft.server.level.ServerPlayer

interface MinigameLobby {
    fun getMap(): PlaceableMap

    fun getSpawn(): Location

    fun getSpawn(player: ServerPlayer): Location {
        return this.getSpawn()
    }

    fun forceTeleport(player: ServerPlayer) {
        player.teleportTo(this.getSpawn(player))
    }

    fun tryTeleport(player: ServerPlayer): Boolean {
        if (player.level() != this.getSpawn().level || !this.getMap().getEntityBoundingBox().contains(player.position())) {
            this.forceTeleport(player)
            return true
        }
        return false
    }
}