package net.casual.arcade.minigame.events.lobby

import net.casual.arcade.area.PlaceableArea
import net.casual.arcade.gui.bossbar.TimerBossBar
import net.casual.arcade.gui.countdown.Countdown
import net.casual.arcade.utils.PlayerUtils.teleportTo
import net.casual.arcade.utils.impl.Location
import net.minecraft.server.level.ServerPlayer

public interface Lobby {
    public val area: PlaceableArea
    public val spawn: Location

    public fun getCountdown(): Countdown

    public fun createBossbar(): TimerBossBar

    public fun getSpawn(player: ServerPlayer): Location {
        return this.spawn
    }

    public fun forceTeleportToSpawn(player: ServerPlayer) {
        player.teleportTo(this.getSpawn(player))
    }

    public fun tryTeleportToSpawn(player: ServerPlayer): Boolean {
        if (player.level() != this.spawn.level || !this.area.getEntityBoundingBox().contains(player.position())) {
            this.forceTeleportToSpawn(player)
            return true
        }
        return false
    }
}