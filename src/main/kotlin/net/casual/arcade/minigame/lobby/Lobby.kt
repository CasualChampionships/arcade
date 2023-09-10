package net.casual.arcade.minigame.lobby

import net.casual.arcade.area.PlaceableArea
import net.casual.arcade.utils.impl.Location
import net.casual.arcade.gui.countdown.Countdown
import net.casual.arcade.utils.PlayerUtils.teleportTo
import net.minecraft.server.level.ServerPlayer
import org.jetbrains.annotations.ApiStatus.Experimental

@Experimental
interface Lobby {
    val area: PlaceableArea
    val spawn: Location

    fun getCountdown(): Countdown

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