package net.casual.arcade.minigame.lobby

import net.casual.arcade.minigame.area.PlaceableArea
import net.casual.arcade.utils.PlayerUtils.teleportTo
import net.casual.arcade.utils.impl.Location
import net.casual.arcade.visuals.bossbar.TimerBossbar
import net.casual.arcade.visuals.countdown.Countdown
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import java.util.*

public interface Lobby {
    public val area: PlaceableArea
    public val spawn: Location

    public fun getCountdown(): Countdown

    public fun createBossbar(): TimerBossbar

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

    public fun createMinigame(server: MinecraftServer): LobbyMinigame {
        return LobbyMinigame(server, UUID.randomUUID(), this)
    }
}